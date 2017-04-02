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

import com.hp.application.automation.tools.octane.actions.dto.AutomatedTest;
import com.hp.application.automation.tools.octane.actions.dto.AutomatedTests;
import com.hp.application.automation.tools.octane.actions.dto.TestFramework;
import com.hp.application.automation.tools.octane.actions.dto.TestingToolType;
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
import org.apache.commons.lang.StringUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

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


    public static UFTTestDetectionResult startScanning(AbstractBuild<?, ?> build, String workspaceId, BuildListener buildListener) {
        ServerConfiguration serverConfiguration = ConfigurationService.getServerConfiguration();
        MqmRestClient client = createClient(serverConfiguration);
        String serverURL = getServerURL(workspaceId, serverConfiguration.sharedSpace, serverConfiguration.location);

        ChangeLogSet<? extends ChangeLogSet.Entry> changeSet = build.getChangeSet();
        Object[] changeSetItems = changeSet.getItems();
        UFTTestDetectionResult result = null;

        try {

            if (!isInitialDetectionDone(build.getWorkspace())) {
                printToConsole(buildListener, "Executing initial detection");
                result = doInitialDetection(client, serverURL, build.getWorkspace(), workspaceId);

                printToConsole(buildListener, String.format("Found %s tests", result.getNewTests().size()));
            } else {
                printToConsole(buildListener, "Executing ChangeSetDetection");
                result = doChangeSetDetection(client, serverURL, changeSetItems, build.getWorkspace(), workspaceId);
            }

            publishDetectionResults(build, buildListener, result);

        } catch (InterruptedException | IOException e) {
            e.printStackTrace();
        }

        return result;
    }

    private static void printToConsole(BuildListener buildListener, String msg) {
        buildListener.getLogger().println("UFTTestDetectionBuildAction : " + msg);
        logger.info(msg);
    }

    private static UFTTestDetectionResult doChangeSetDetection(MqmRestClient client, String serverURL, Object[] changeSetItems, FilePath workspace, String workspaceId) throws IOException, InterruptedException {
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
                if (EditType.ADD.equals(path.getEditType())) {
                    if (isTestMainFilePath(path.getPath())) {
                        String filePath = workspace + File.separator + path.getPath();
                        if (isFileExist(filePath)) {
                            FilePath testFolder = getTestFolderForTestMainFile(filePath);
                            scanFileSystemRecursively(workspace, testFolder, result);
                        }

                    }
                } else if (EditType.DELETE.equals(path.getEditType())) {
                    if (isTestMainFilePath(path.getPath())) {
                        String filePath = workspace + File.separator + path.getPath();
                        if (!isFileExist(filePath)) {
                            FilePath testFolder = getTestFolderForTestMainFile(filePath);
                            AutomatedTest test = createAutomatedTest(workspace, testFolder);
                            result.addDeletedTest(test);
                        }
                    }

                }
            }
        }

        deleteTests(client, result.getDeletedTests(), workspaceId);
        postTests(client, serverURL, result.getNewTests(), workspaceId);

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

    private static boolean isInitialDetectionDone(FilePath workspace) {
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

    private static UFTTestDetectionResult doInitialDetection(MqmRestClient client, String serverURL, FilePath workspace, String workspaceId) throws IOException, InterruptedException {
        UFTTestDetectionResult result = new UFTTestDetectionResult();

        scanFileSystemRecursively(workspace, workspace, result);

        postTests(client, serverURL, result.getNewTests(), workspaceId);


        File rootFile = new File(workspace.toURI());
        File file = new File(rootFile, INITIAL_DETECTION_FILE);
        file.createNewFile();

        return result;
    }

    private static void scanFileSystemRecursively(FilePath root, FilePath dirPath, UFTTestDetectionResult result) throws IOException, InterruptedException {
        List<FilePath> paths = dirPath.list();


        //if it test folder - create new test, else drill down to subFolders
        if (isUftTestFolder(paths)) {
            AutomatedTest test = createAutomatedTest(root, dirPath);

            result.addNewTest(test);

        } else {
            for (FilePath path : paths) {
                if (path.isDirectory()) {
                    scanFileSystemRecursively(root, path, result);
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

    private static void postTests(MqmRestClient client, String serverURL, List<AutomatedTest> tests, String workspaceId) throws UnsupportedEncodingException {
        if (tests.isEmpty()) {
            return;
        }

        completeUftProperties(client, Long.parseLong(workspaceId), tests);

        int BULK_SIZE = 100;
        for (int i = 0; i < tests.size(); i += BULK_SIZE)
            try {
                AutomatedTests data = AutomatedTests.createWithTests(tests.subList(i, Math.min(i + BULK_SIZE, tests.size())));
                String uftTestJson = JSONObject.fromObject(data).toString();
                client.postTest(uftTestJson, null, serverURL);
                //JSONObject testObject = (JSONObject) jsonObject.getJSONArray("data").get(0);

            } catch (RequestErrorException e) {
                if (e.getStatusCode() != RESPONSE_STATUS_CONFLICT) {
                    throw e;
                }
                //else :  the test with the same hash code , so do nothing
            }
    }

    private static void deleteTests(MqmRestClient client, Collection<AutomatedTest> removedTests, String workspaceId) throws UnsupportedEncodingException {
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
    }


    private static void completeUftProperties(MqmRestClient client, long workspaceId, Collection<AutomatedTest> tests) {
        TestingToolType uftTestingTool = getUftTestingTool(client, workspaceId);
        TestFramework uftFramework = getUftFramework(client, workspaceId);
        for (AutomatedTest test : tests) {
            test.setTesting_tool_type(uftTestingTool);
            test.setFramework(uftFramework);
        }
    }

    private static boolean isUftTestFolder(List<FilePath> paths) {
        for (FilePath path : paths) {
            if (path.getName().endsWith(STFileExtention) || path.getName().endsWith(QTPFileExtention))
                return true;
        }

        return false;
    }

    private static boolean isTestMainFilePath(String path) {
        String lowerPath = path.toLowerCase();
        boolean isMainFile = lowerPath.endsWith(STFileExtention) || lowerPath.endsWith(QTPFileExtention);
        return isMainFile;
    }

    private static FilePath getTestFolderForTestMainFile(String path) {
        if (isTestMainFilePath(path)) {
            File file = new File(path);
            File parent = file.getParentFile();
            return new FilePath(parent);
        }
        return null;
    }

    private static TestingToolType getUftTestingTool(MqmRestClient client, long workspaceId) {
        PagedList<ListItem> testingTools = client.queryListItems("list_node.testing_tool_type", null, workspaceId, 0, 100);
        String uftTestingToolLogicalName = "list_node.testing_tool_type.uft";

        for (ListItem item : testingTools.getItems()) {
            if (uftTestingToolLogicalName.equals(item.getLogicalName())) {
                return TestingToolType.fromListItem(item);
            }
        }
        return null;
    }

    private static TestFramework getUftFramework(MqmRestClient client, long workspaceId) {
        PagedList<ListItem> testingTools = client.queryListItems("list_node.je.framework", null, workspaceId, 0, 100);
        String uftTestingToolLogicalName = "list_node.je.framework.uft";

        for (ListItem item : testingTools.getItems()) {
            if (uftTestingToolLogicalName.equals(item.getLogicalName())) {
                return TestFramework.fromListItem(item);
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


        for (AutomatedTest test : detectionResult.getNewTests()) {
            Element elmTest = doc.createElement("test");
            elmTest.setAttribute("name", test.getName());
            elmTest.setAttribute("package", test.getPackage());
            elmTest.setAttribute("status", "new");
            root.appendChild(elmTest);
        }

        for (AutomatedTest test : detectionResult.getDeletedTests()) {
            Element elmTest = doc.createElement("test");
            elmTest.setAttribute("name", test.getName());
            elmTest.setAttribute("package", test.getPackage());
            elmTest.setAttribute("status", "deleted");
            root.appendChild(elmTest);
        }


        try {
            write2XML(doc, fileName);
        } catch (TransformerException e) {
            _logger.error("Failed transforming xml file: " + e);
        } catch (FileNotFoundException e) {
            _logger.error("Failed to find " + fileName + ": " + e);
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

    public static UFTTestDetectionResult readReportFromXMLFile(AbstractBuild<?, ?> build) throws ParserConfigurationException, IOException, SAXException {

        UFTTestDetectionResult result = new UFTTestDetectionResult();
        String fileName = getReportXmlFileName(build);

        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder;
        Document doc;
        builder = dbf.newDocumentBuilder();
        doc = builder.parse(fileName);

        Element root = doc.getDocumentElement();
        NodeList testsList = root.getElementsByTagName("tests");
        for (int i = 0; i < testsList.getLength(); i++) {
            Element testEl = (Element) testsList.item(i);
            String testName = testEl.getAttribute("name");
            String packageName = testEl.getAttribute("package");
            String status = testEl.getAttribute("status");

            AutomatedTest test = new AutomatedTest();
            test.setPackage(packageName);
            test.setPackage(testName);

            if ("new".equalsIgnoreCase(status)) {
                result.addNewTest(test);
            } else if ("deleted".equalsIgnoreCase(status)) {
                result.addDeletedTest(test);
            }


        }

        return result;

    }
}