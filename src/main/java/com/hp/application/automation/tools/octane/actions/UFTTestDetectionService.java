/*
 *     Copyright 2017 Hewlett-Packard Development Company, L.P.
 *     Licensed under the Apache License, Version 2.0 (the "License");
 *     you may not use this file except in compliance with the License.
 *     You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS,
 *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *     See the License for the specific language governing permissions and
 *     limitations under the License.
 *
 */

package com.hp.application.automation.tools.octane.actions;

import com.hp.application.automation.tools.octane.actions.dto.*;
import com.hp.application.automation.tools.octane.client.JenkinsMqmRestClientFactory;
import com.hp.application.automation.tools.octane.configuration.ConfigurationService;
import com.hp.application.automation.tools.octane.configuration.ServerConfiguration;
import com.hp.mqm.client.MqmRestClient;
import com.hp.mqm.client.exception.RequestErrorException;
import com.hp.mqm.client.model.ListItem;
import com.hp.mqm.client.model.PagedList;
import com.hp.mqm.client.model.Test;
import hudson.ExtensionList;
import hudson.FilePath;
import hudson.model.AbstractBuild;
import hudson.model.BuildListener;
import hudson.model.TaskListener;
import hudson.plugins.git.GitChangeSet;
import hudson.scm.ChangeLogSet;
import hudson.scm.EditType;
import jenkins.model.Jenkins;
import net.sf.json.JSONObject;
import net.sf.json.JsonConfig;
import net.sf.json.processors.PropertyNameProcessor;
import net.sf.json.util.PropertyFilter;
import org.apache.commons.lang.StringUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.*;
import java.util.*;

public class UFTTestDetectionService {

    private static final java.util.logging.Logger logger = java.util.logging.Logger.getLogger(UFTTestDetectionService.class.getName());
    private static final String INITIAL_DETECTION_FILE = "INITIAL_DETECTION_FILE.txt";
    private static final String DETECTION_RESULT_FILE = "detection_result.xml";
    private static final String STFileExtention = ".st";//api test
    private static final String QTPFileExtention = ".tsp";//gui test
    private final static int RESPONSE_STATUS_CONFLICT = 409;


    public static UFTTestDetectionResult startScanning(AbstractBuild<?, ?> build, String workspaceId, String scmResourceId, BuildListener buildListener) {
        ServerConfiguration serverConfiguration = ConfigurationService.getServerConfiguration();
        MqmRestClient client = createClient(serverConfiguration);
        String serverURL = getServerURL(workspaceId, serverConfiguration.sharedSpace, serverConfiguration.location);

        ChangeLogSet<? extends ChangeLogSet.Entry> changeSet = build.getChangeSet();
        Object[] changeSetItems = changeSet.getItems();
        UFTTestDetectionResult result = null;

        try {

            boolean initialDetection = !initialDetectionFileExist(build.getWorkspace());
            if (initialDetection) {
                printToConsole(buildListener, "Executing initial detection");
                result = doInitialDetection(build.getWorkspace());
            } else {
                printToConsole(buildListener, "Executing changeSet detection");
                result = doChangeSetDetection(changeSetItems, build.getWorkspace());
                removeTestDuplicated(result.getUpdatedTests());
            }
            printToConsole(buildListener, String.format("Found %s new tests", result.getNewTests().size()));
            printToConsole(buildListener, String.format("Found %s updated tests", result.getUpdatedTests().size()));

            //post new tests
            if (!result.getNewTests().isEmpty()) {
                boolean posted = postTests(client, serverURL, result.getNewTests(), workspaceId, scmResourceId);
                result.setPostedSuccessfully(posted);

                //create initial detection file
                if (initialDetection) {
                    File rootFile = new File(build.getWorkspace().toURI());
                    File file = new File(rootFile, INITIAL_DETECTION_FILE);
                    file.createNewFile();
                }

                printToConsole(buildListener, "New tests posted successfully = " + result.isPostedSuccessfully());
            }
            //post updated
            if (!result.getUpdatedTests().isEmpty()) {
                boolean updated = updateTests(client, result.getUpdatedTests(), workspaceId);
                result.setUpdatedSuccessfully(updated);
                printToConsole(buildListener, "Updated successfully = " + result.isUpdatedSuccessfully());
            }


            publishDetectionResults(build, buildListener, result);
        } catch (InterruptedException | IOException e) {
            e.printStackTrace();
        }

        return result;
    }

    private static void removeTestDuplicated(List<AutomatedTest> tests) {
        Set<String> keys = new HashSet<>();
        List<AutomatedTest> testsToRemove = new ArrayList<>();
        for (AutomatedTest test : tests) {
            String key = test.getPackage() + "_" + test.getName();
            if (keys.contains(key)) {
                testsToRemove.add(test);
            }
            keys.add(key);

        }
        tests.removeAll(testsToRemove);
    }

    private static void printToConsole(BuildListener buildListener, String msg) {
        if (buildListener != null) {
            buildListener.getLogger().println("UFTTestDetectionService : " + msg);
        }

        logger.info(msg);
    }

    private static UFTTestDetectionResult doChangeSetDetection(Object[] changeSetItems, FilePath workspace) throws IOException, InterruptedException {
        UFTTestDetectionResult result = new UFTTestDetectionResult();
        if (changeSetItems.length == 0) {
            return result;
        }

        boolean isGitChanges = changeSetItems[0] instanceof GitChangeSet;
        if (!isGitChanges) {
            return result;
        }

        for (int i = 0; i < changeSetItems.length; i++) {
            GitChangeSet changeSet = (GitChangeSet) changeSetItems[i];
            for (GitChangeSet.Path path : changeSet.getPaths()) {
                if (isTestMainFilePath(path.getPath())) {
                    String filePath = workspace + File.separator + path.getPath();

                    if (EditType.ADD.equals(path.getEditType())) {
                        if (isFileExist(filePath)) {
                            FilePath testFolder = getTestFolderForTestMainFile(filePath);
                            scanFileSystemRecursively(workspace, testFolder, result.getNewTests());
                        }
                    } else if (EditType.DELETE.equals(path.getEditType())) {
                        if (!isFileExist(filePath)) {
                            FilePath testFolder = getTestFolderForTestMainFile(filePath);
                            AutomatedTest test = createAutomatedTest(workspace, testFolder);
                            result.getDeletedTests().add(test);
                        }
                    } else if (EditType.EDIT.equals(path.getEditType())) {
                        if (isFileExist(filePath)) {
                            FilePath testFolder = getTestFolderForTestMainFile(filePath);
                            scanFileSystemRecursively(workspace, testFolder, result.getUpdatedTests());
                        }
                    }
                }
            }
        }

        return result;
    }

    private static AutomatedTest createAutomatedTest(FilePath root, FilePath dirPath) throws IOException, InterruptedException {
        AutomatedTest test = new AutomatedTest();
        test.setName(dirPath.getName());

        //set component - relative path from root
        String testPath = dirPath.toURI().toString();
        String rootPath = root.toURI().toString();
        String path = testPath.replace(rootPath, "");
        path = StringUtils.strip(path, "\\/");
        String _package = path.length() != dirPath.getName().length() ? path.substring(0, path.length() - dirPath.getName().length() - 1) : "";
        test.setPackage(_package);
        return test;
    }

    private static boolean isFileExist(String path) {
        File file = new File(path);
        return file.exists();
    }

    private static boolean initialDetectionFileExist(FilePath workspace) {
        try {
            File rootFile = new File(workspace.toURI());
            File file = new File(rootFile, INITIAL_DETECTION_FILE);
            return file.exists();

        } catch (Exception e) {
            return false;
        }
    }

    public static void removeInitialDetectionFlag(FilePath workspace) throws IOException, InterruptedException {
        File rootFile = new File(workspace.toURI());
        File file = new File(rootFile, INITIAL_DETECTION_FILE);
        file.delete();
    }

    private static UFTTestDetectionResult doInitialDetection(FilePath workspace) throws IOException, InterruptedException {
        UFTTestDetectionResult result = new UFTTestDetectionResult();

        scanFileSystemRecursively(workspace, workspace, result.getNewTests());

        return result;
    }

    private static void scanFileSystemRecursively(FilePath root, FilePath dirPath, List<AutomatedTest> foundTests) throws IOException, InterruptedException {
        List<FilePath> paths = dirPath.list();

        //if it test folder - create new test, else drill down to subFolders
        UftTestType testType = isUftTestFolder(paths);
        if (!testType.isNone()) {
            AutomatedTest test = createAutomatedTest(root, dirPath);
            test.setUftTestType(testType);
            String description = UFTTestUtil.getTestDescription(dirPath);
            test.setDescription(description);
            foundTests.add(test);

        } else {
            for (FilePath path : paths) {
                if (path.isDirectory()) {
                    scanFileSystemRecursively(root, path, foundTests);
                }
            }
        }
    }

    private static MqmRestClient createClient(ServerConfiguration configuration) {
        JenkinsMqmRestClientFactory clientFactory = getExtension(JenkinsMqmRestClientFactory.class);
        MqmRestClient client = clientFactory.obtain(
                configuration.location,
                configuration.sharedSpace,
                configuration.username,
                configuration.password);
        return client;
    }

    private static boolean postTests(MqmRestClient client, String serverURL, List<AutomatedTest> tests, String workspaceId, String scmResourceId) throws UnsupportedEncodingException {

        if (!tests.isEmpty()) {
            try {
                completeTestProperties(client, Long.parseLong(workspaceId), tests, scmResourceId);
            } catch (RequestErrorException e) {
                logger.warning("Failed to completeTestProperties : " + e.getMessage());
                return false;
            }

            int BULK_SIZE = 100;
            for (int i = 0; i < tests.size(); i += BULK_SIZE)
                try {
                    AutomatedTests data = AutomatedTests.createWithTests(tests.subList(i, Math.min(i + BULK_SIZE, tests.size())));
                    String uftTestJson = convertToJsonString(data);

                    client.postTest(uftTestJson, null, serverURL);
                    //JSONObject testObject = (JSONObject) jsonObject.getJSONArray("data").get(0);

                } catch (RequestErrorException e) {
                    if (e.getStatusCode() != RESPONSE_STATUS_CONFLICT) {
                        logger.warning("Failed to postTests to Octane : " + e.getMessage());
                        return false;
                    }

                    //else :  the test with the same hash code , so do nothing
                }
        }
        return true;
    }

    private static String convertToJsonString(AutomatedTests data) {
        JsonConfig config = getJsonConfig();
        return JSONObject.fromObject(data, config).toString();
    }

    private static String convertToJsonString(AutomatedTest test) {
        JsonConfig config = getJsonConfig();
        return JSONObject.fromObject(test, config).toString();
    }

    private static JsonConfig getJsonConfig() {
        JsonConfig config = new JsonConfig();
        //override field names
        config.registerJsonPropertyNameProcessor(AutomatedTest.class, new PropertyNameProcessor() {

            @Override
            public String processPropertyName(Class className, String fieldName) {
                String result = fieldName;
                switch (fieldName) {
                    case "scmRepository":
                        result = "scm_repository";
                        break;
                    case "testingToolType":
                        result = "testing_tool_type";
                        break;
                    case "testTypes":
                        result = "test_type";
                        break;
                    default:
                        break;
                }
                return result;
            }
        });

        //filter empty fields
        PropertyFilter pf = new PropertyFilter() {
            public boolean apply(Object source, String name, Object value) {
                if (value != null) {
                    return false;
                }
                return true;
            }
        };
        config.setJsonPropertyFilter(pf);

        //skip fields
        config.registerPropertyExclusion(AutomatedTest.class, "uftTestType");
        return config;
    }

    /*private static void deleteTests(MqmRestClient client, Collection<AutomatedTest> removedTests, String workspaceId) throws UnsupportedEncodingException {
        List<Long> idsToDelete = new ArrayList<>();
        long workspaceIdAsLong = Long.parseLong(workspaceId);
        for (AutomatedTest test : removedTests) {
            Map<String, String> queryFields = new HashMap<>();
            queryFields.put("name", test.getName());
            queryFields.put("package", test.getPackage());
            PagedList<Test> foundTests = client.getTests(workspaceIdAsLong, queryFields, Arrays.asList("id"));
            if (foundTests.getItems().size() == 1) {
                idsToDelete.add(foundTests.getItems().get(0).getId());
            }
        }

        int BULK_SIZE = 100;
        for (int i = 0; i < idsToDelete.size(); i += BULK_SIZE) {
            client.deleteTests(workspaceIdAsLong, idsToDelete.subList(i, Math.min(i + BULK_SIZE, idsToDelete.size())));
        }
    }*/

    private static boolean updateTests(MqmRestClient client, Collection<AutomatedTest> updateTests, String workspaceId) throws UnsupportedEncodingException {
        long workspaceIdAsLong = Long.parseLong(workspaceId);

        try {
            for (AutomatedTest test : updateTests) {
                if (StringUtils.isEmpty(test.getDescription())) {
                    continue;
                }
                Map<String, String> queryFields = new HashMap<>();
                queryFields.put("name", test.getName());
                queryFields.put("package", test.getPackage());
                PagedList<Test> foundTests = client.getTests(workspaceIdAsLong, queryFields, Arrays.asList("id, description"));
                if (foundTests.getItems().size() == 1) {
                    Test foundTest = foundTests.getItems().get(0);
                    AutomatedTest testForUpdate = new AutomatedTest();
                    testForUpdate.setSubtype(null);
                    testForUpdate.setDescription(test.getDescription());
                    testForUpdate.setId(foundTest.getId());
                    String json = convertToJsonString(testForUpdate);
                    client.updateTest(Long.parseLong(workspaceId), foundTests.getItems().get(0).getId(), json);
                }
            }
            return true;

        } catch (Exception e) {
            return false;
        }
    }

    private static void completeTestProperties(MqmRestClient client, long workspaceId, Collection<AutomatedTest> tests, String scmResourceId) {
        ListNodeEntity uftTestingTool = getUftTestingTool(client, workspaceId);
        ListNodeEntity uftFramework = getUftFramework(client, workspaceId);
        ListNodeEntity guiTestType = hasTestsByType(tests, UftTestType.GUI) ? getGuiTestType(client, workspaceId) : null;
        ListNodeEntity apiTestType = hasTestsByType(tests, UftTestType.API) ? getApiTestType(client, workspaceId) : null;

        BaseRefEntity scmRepository = StringUtils.isEmpty(scmResourceId) ? null : BaseRefEntity.create("scm_repository", Long.valueOf(scmResourceId));
        for (AutomatedTest test : tests) {
            test.setTestingToolType(uftTestingTool);
            test.setFramework(uftFramework);
            test.setScmRepository(scmRepository);

            ListNodeEntity testType = guiTestType;
            if (test.getUftTestType().equals(UftTestType.API)) {
                testType = apiTestType;
            }
            test.setTestTypes(ListNodeEntityCollection.create(testType));
        }
    }

    private static boolean hasTestsByType(Collection<AutomatedTest> tests, UftTestType uftTestType) {
        for (AutomatedTest test : tests) {
            if (uftTestType.equals(test.getUftTestType())) {
                return true;
            }
        }
        return false;
    }

    private static UftTestType isUftTestFolder(List<FilePath> paths) {
        for (FilePath path : paths) {
            if (path.getName().endsWith(STFileExtention)) {
                return UftTestType.API;
            }
            if (path.getName().endsWith(QTPFileExtention)) {
                return UftTestType.GUI;
            }
        }

        return UftTestType.None;
    }

    private static boolean isTestMainFilePath(String path) {
        String lowerPath = path.toLowerCase();
        if (lowerPath.endsWith(STFileExtention)) {
            return true;
        }
        else if (lowerPath.endsWith(QTPFileExtention)) {
            return true;
        }

        return false;
    }

    private static FilePath getTestFolderForTestMainFile(String path) {
        if (isTestMainFilePath(path)) {
            File file = new File(path);
            File parent = file.getParentFile();
            return new FilePath(parent);
        }
        return null;
    }

    private static ListNodeEntity getUftTestingTool(MqmRestClient client, long workspaceId) {
        PagedList<ListItem> testingTools = client.queryListItems("list_node.testing_tool_type", null, workspaceId, 0, 100);
        String uftTestingToolLogicalName = "list_node.testing_tool_type.uft";

        for (ListItem item : testingTools.getItems()) {
            if (uftTestingToolLogicalName.equals(item.getLogicalName())) {
                return ListNodeEntity.create(item.getId());
            }
        }
        return null;
    }

    private static ListNodeEntity getUftFramework(MqmRestClient client, long workspaceId) {
        PagedList<ListItem> testingTools = client.queryListItems("list_node.je.framework", null, workspaceId, 0, 100);
        String uftTestingToolLogicalName = "list_node.je.framework.uft";

        for (ListItem item : testingTools.getItems()) {
            if (uftTestingToolLogicalName.equals(item.getLogicalName())) {
                return ListNodeEntity.create(item.getId());
            }
        }
        return null;
    }

    private static ListNodeEntity getGuiTestType(MqmRestClient client, long workspaceId) {
        PagedList<ListItem> testingTools = client.queryListItems("list_node.test_type", null, workspaceId, 0, 100);
        String guiLogicalName = "list_node.test_type.gui";

        for (ListItem item : testingTools.getItems()) {
            if (guiLogicalName.equals(item.getLogicalName())) {
                return ListNodeEntity.create(item.getId());
            }
        }
        return null;
    }

    private static ListNodeEntity getApiTestType(MqmRestClient client, long workspaceId) {
        PagedList<ListItem> testingTools = client.queryListItems("list_node.test_type", null, workspaceId, 0, 100);
        String guiLogicalName = "list_node.test_type.api";

        for (ListItem item : testingTools.getItems()) {
            if (guiLogicalName.equals(item.getLogicalName())) {
                return ListNodeEntity.create(item.getId());
            }
        }
        return null;
    }


    private static String getServerURL(String workspaceId, String sharedspaceId, String location) {
        return location + "/api/shared_spaces/" + sharedspaceId + "/workspaces/" + workspaceId;
    }

    private static <T> T getExtension(Class<T> clazz) {
        ExtensionList<T> items = Jenkins.getInstance().getExtensionList(clazz);
        return items.get(0);
    }

    private static void publishDetectionResults(AbstractBuild<?, ?> build, TaskListener _logger, UFTTestDetectionResult detectionResult) {

        String fileName = getReportXmlFileName(build);

        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = null;
        try {
            builder = dbf.newDocumentBuilder();
        } catch (ParserConfigurationException e) {
            _logger.error("Failed creating xml doc report: " + e);
            return;
        }
        Document doc = builder.newDocument();
        Element root = doc.createElement("uft_test_scanning_results");
        doc.appendChild(root);

        root.setAttribute("new", String.valueOf(detectionResult.getNewTests().size()));
        root.setAttribute("deleted", String.valueOf(detectionResult.getDeletedTests().size()));
        root.setAttribute("updated", String.valueOf(detectionResult.getUpdatedTests().size()));
        root.setAttribute("postedSuccessfully", String.valueOf(detectionResult.isPostedSuccessfully()));

        addTestElement(detectionResult.getNewTests(), doc, root, "new");
        addTestElement(detectionResult.getDeletedTests(), doc, root, "deleted");
        addTestElement(detectionResult.getUpdatedTests(), doc, root, "updated");

        try {
            write2XML(doc, fileName);
        } catch (TransformerException e) {
            _logger.error("Failed transforming xml file: " + e);
        } catch (FileNotFoundException e) {
            _logger.error("Failed to find " + fileName + ": " + e);
        }
    }

    private static void addTestElement(List<AutomatedTest> tests, Document doc, Element root, String status) {
        for (AutomatedTest test : tests) {
            Element elmTest = doc.createElement("test");
            elmTest.setAttribute("name", test.getName());
            elmTest.setAttribute("package", test.getPackage());
            elmTest.setAttribute("status", status);
            root.appendChild(elmTest);
        }
    }

    private static String getReportXmlFileName(AbstractBuild<?, ?> build) {
        File reportXmlFile = new File(build.getRootDir(), DETECTION_RESULT_FILE);
        return reportXmlFile.getAbsolutePath();
    }

    private static void write2XML(Document document, String filename) throws TransformerException, FileNotFoundException {
        document.normalize();

        TransformerFactory tFactory = TransformerFactory.newInstance();
        Transformer transformer = tFactory.newTransformer();
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");

        DOMSource source = new DOMSource(document);
        PrintWriter pw = new PrintWriter(new FileOutputStream(filename));
        StreamResult result = new StreamResult(pw);
        transformer.transform(source, result);

    }
}