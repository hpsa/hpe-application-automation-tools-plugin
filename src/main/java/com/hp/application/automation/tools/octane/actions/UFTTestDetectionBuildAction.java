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
import hudson.model.Action;
import hudson.model.BuildListener;
import hudson.plugins.git.GitChangeSet;
import hudson.scm.ChangeLogSet;
import hudson.scm.EditType;
import jenkins.model.Jenkins;
import net.sf.json.JSONObject;
import org.apache.commons.lang.StringUtils;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.*;

        
public class UFTTestDetectionBuildAction implements Action {
    private final static int RESPONSE_STATUS_CONFLICT=409;
    private AbstractBuild<?, ?> build;
    private String workspaceId;
    private BuildListener buildListener;
    private static final java.util.logging.Logger logger = java.util.logging.Logger.getLogger(UFTTestDetectionBuildAction.class.getName());


    private final String INITIAL_DETECTION_FILE = "INITIAL_DETECTION_FILE.txt";
    private final String STFileExtention = ".st";//api test
    private final String QTPFileExtention = ".tsp";//gui test

    @Override
    public String getIconFileName() {
        return "notepad.png";
    }

    @Override
    public String getDisplayName() {
        return "HP Octane UFT Tests Scanner Report";
    }

    @Override
    public String getUrlName() {
        return "uft_report";
    }


    public UFTTestDetectionBuildAction(final AbstractBuild<?, ?> build, String workspaceId, BuildListener buildListener) {
        this.build = build;
        this.workspaceId = workspaceId;
        this.buildListener = buildListener;
    }

    public void startScanning() {
        ServerConfiguration serverConfiguration = ConfigurationService.getServerConfiguration();
        MqmRestClient client = createClient(serverConfiguration);
        String serverURL = getServerURL(workspaceId, serverConfiguration.sharedSpace, serverConfiguration.location);

        ChangeLogSet<? extends ChangeLogSet.Entry> changeSet = build.getChangeSet();
        Object[] changeSetItems = changeSet.getItems();


        try {
            if (!isInitialDetectionDone()) {
                printToConsole("Executing initial detection");
                Collection<AutomatedTest> foundTests = doInitialDetection(client, serverURL);
                printToConsole(String.format("Found %s tests", foundTests.size()));
            } else {
                printToConsole("Executing ChangeSetDetection");
                doChangeSetDetection(client, serverURL, changeSetItems);
            }
        } catch (InterruptedException | IOException e) {
            e.printStackTrace();
        }
    }

    private void printToConsole(String msg) {
        buildListener.getLogger().println("UFTTestDetectionBuildAction : " + msg);
        logger.info(msg);
    }

    private void doChangeSetDetection(MqmRestClient client, String serverURL, Object[] changeSetItems) throws IOException, InterruptedException {
        if (changeSetItems.length == 0) {
            return;
        }

        boolean isGitChanges = changeSetItems[0] instanceof GitChangeSet;
        if (!isGitChanges) {
            printToConsole(String.format("Expected GitChangeSet but found %s, detection is canceled.", changeSetItems[0].getClass().getName()));
            return;
        }

        List<AutomatedTest> addedTests = new ArrayList<>();
        List<AutomatedTest> removedTests = new ArrayList<>();

        for (int i = 0; i < changeSetItems.length; i++) {
            GitChangeSet changeSet = (GitChangeSet) changeSetItems[i];
            for (GitChangeSet.Path path : changeSet.getPaths()) {
                if (EditType.ADD.equals(path.getEditType())) {
                    if (isTestMainFilePath(path.getPath())) {
                        String filePath = build.getWorkspace() + File.separator + path.getPath();
                        if (isFileExist(filePath)) {
                            FilePath testFolder = getTestFolderForTestMainFile(filePath);
                            scanFileSystemRecursively(build.getWorkspace(), testFolder, addedTests);
                        }

                    }
                } else if (EditType.DELETE.equals(path.getEditType())) {
                    if (isTestMainFilePath(path.getPath())) {
                        String filePath = build.getWorkspace() + File.separator + path.getPath();
                        if (!isFileExist(filePath)) {
                            FilePath testFolder = getTestFolderForTestMainFile(filePath);
                            AutomatedTest test = createAutomatedTest(build.getWorkspace(), testFolder);
                            removedTests.add(test);
                        }
                    }

                }
            }
        }

        postTests(client, serverURL, addedTests);
        deleteTests(client, removedTests);

    }

    private AutomatedTest createAutomatedTest(FilePath root, FilePath dirPath) throws IOException, InterruptedException {
        AutomatedTest test = new AutomatedTest();
        test.setName(dirPath.getName());

        //set component - relative path from root
        String testPath = dirPath.toURI().toString();
        String rootPath = root.toURI().toString();
        String path = testPath.replace(rootPath, "");
        path = StringUtils.strip(path, "\\/");
        String component = path.length() != dirPath.getName().length() ? path.substring(0, path.length() - dirPath.getName().length() - 1) : "";
        test.setComponent(component);
        return test;
    }


    private boolean isFileExist(String path) {
        File file = new File(path);
        return file.exists();
    }

    private boolean isInitialDetectionDone() {
        try {
            File rootFile = new File(build.getWorkspace().toURI());
            File file = new File(rootFile, INITIAL_DETECTION_FILE);
            return file.exists();

        } catch (Exception e) {
            return false;
        }
    }

    private Collection<AutomatedTest> doInitialDetection(MqmRestClient client, String serverURL) throws IOException, InterruptedException {
        List<AutomatedTest> tests = new ArrayList<>();

        scanFileSystemRecursively(build.getWorkspace(), build.getWorkspace(), tests);

        postTests(client, serverURL, tests);


        File rootFile = new File(build.getWorkspace().toURI());
        File file = new File(rootFile, INITIAL_DETECTION_FILE);
        file.createNewFile();


        return tests;
    }

    private void scanFileSystemRecursively(FilePath root, FilePath dirPath, List<AutomatedTest> tests) throws IOException, InterruptedException {
        List<FilePath> paths = dirPath.list();


        //if it test folder - create new test, else drill down to subFolders
        if (isUftTestFolder(paths)) {
            AutomatedTest test = createAutomatedTest(root, dirPath);

            tests.add(test);

        } else {
            for (FilePath path : paths) {
                if (path.isDirectory()) {
                    scanFileSystemRecursively(root, path, tests);
                }
            }
        }
    }

    private MqmRestClient createClient(ServerConfiguration configuration) {
        JenkinsMqmRestClientFactory clientFactory = getExtension(JenkinsMqmRestClientFactory.class);
        MqmRestClient client = clientFactory.obtain(
                configuration.location,
                configuration.sharedSpace,
                configuration.username,
                configuration.password);
        return client;
    }

    private void postTests(MqmRestClient client, String serverURL, List<AutomatedTest> tests) throws UnsupportedEncodingException {
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
            //TODO: replace with constant from common lib
                if (e.getStatusCode() != RESPONSE_STATUS_CONFLICT);{ // "Conflict" parallel to : Response.Status.CONFLICT.getStatusCode()) {
                    throw e;
                }
                //else :  the test with the same hash code , so do nothing
            }
    }

    private void deleteTests(MqmRestClient client, List<AutomatedTest> removedTests) throws UnsupportedEncodingException {
        List<Long> idsToDelete = new ArrayList<>();
        long workspaceIdAsLong = Long.parseLong(workspaceId);
        for (AutomatedTest test : removedTests) {
            Map<String, String> queryFields = new HashMap<>();
            queryFields.put("name", test.getName());
            queryFields.put("component", test.getComponent());
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


    private void completeUftProperties(MqmRestClient client, long workspaceId, Collection<AutomatedTest> tests) {
        TestingToolType uftTestingTool = getUftTestingTool(client, workspaceId);
        TestFramework uftFramework = getUftFramework(client, workspaceId);
        for (AutomatedTest test : tests) {
            test.setTesting_tool_type(uftTestingTool);
            test.setFramework(uftFramework);
        }
    }

    public boolean isUftTestFolder(List<FilePath> paths) {
        for (FilePath path : paths) {
            if (path.getName().endsWith(STFileExtention) || path.getName().endsWith(QTPFileExtention))
                return true;
        }

        return false;
    }

    public boolean isTestMainFilePath(String path) {
        String lowerPath = path.toLowerCase();
        boolean isMainFile = lowerPath.endsWith(STFileExtention) || lowerPath.endsWith(QTPFileExtention);
        return isMainFile;
    }

    public FilePath getTestFolderForTestMainFile(String path) {
        if (isTestMainFilePath(path)) {
            File file = new File(path);
            File parent = file.getParentFile();
            return new FilePath(parent);
        }
        return null;
    }


    private TestingToolType getUftTestingTool(MqmRestClient client, long workspaceId) {
        PagedList<ListItem> testingTools = client.queryListItems("list_node.testing_tool_type", null, workspaceId, 0, 100);
        String uftTestingToolLogicalName = "list_node.testing_tool_type.uft";

        for (ListItem item : testingTools.getItems()) {
            if (uftTestingToolLogicalName.equals(item.getLogicalName())) {
                return TestingToolType.fromListItem(item);
            }
        }
        return null;
    }

    private TestFramework getUftFramework(MqmRestClient client, long workspaceId) {
        PagedList<ListItem> testingTools = client.queryListItems("list_node.je.framework", null, workspaceId, 0, 100);
        String uftTestingToolLogicalName = "list_node.je.framework.uft";

        for (ListItem item : testingTools.getItems()) {
            if (uftTestingToolLogicalName.equals(item.getLogicalName())) {
                return TestFramework.fromListItem(item);
            }
        }
        return null;
    }

    private String getServerURL(String workspaceId, String sharedspaceId, String location) {
        return location + "/api/shared_spaces/" + sharedspaceId + "/workspaces/" + workspaceId;
    }

    private static <T> T getExtension(Class<T> clazz) {
        ExtensionList<T> items = Jenkins.getInstance().getExtensionList(clazz);
        return items.get(0);
    }

}