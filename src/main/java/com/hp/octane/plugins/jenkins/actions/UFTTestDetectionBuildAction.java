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

package com.hp.octane.plugins.jenkins.actions;

import com.hp.mqm.client.MqmRestClient;
import com.hp.octane.plugins.jenkins.actions.dto.AutomatedTest;
import com.hp.octane.plugins.jenkins.actions.dto.AutomatedTests;
import com.hp.octane.plugins.jenkins.client.JenkinsMqmRestClientFactory;
import com.hp.octane.plugins.jenkins.configuration.ConfigurationService;
import com.hp.octane.plugins.jenkins.configuration.ServerConfiguration;
import hudson.ExtensionList;
import hudson.FilePath;
import hudson.model.AbstractBuild;
import hudson.model.Action;
import jenkins.model.Jenkins;
import net.sf.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;


public class UFTTestDetectionBuildAction implements Action {
    private String message;
    private AbstractBuild<?, ?> build;
    private static final java.util.logging.Logger logger = java.util.logging.Logger.getLogger(UFTTestDetectionBuildAction.class.getName());

    @Override
    public String getIconFileName() {
        return "/plugin/testExample/img/build-goals.png";
    }

    @Override
    public String getDisplayName() {
        return "Test Example Build Page";
    }

    @Override
    public String getUrlName() {
        return "testExampleBA";
    }

    public String getMessage() {
        return this.message;
    }

    public int getBuildNumber() {
        return this.build.number;
    }

    public AbstractBuild<?, ?> getBuild() {
        return build;
    }

    private void findUFTTestsPath(List<FilePath> root, HashMap<String, String> testData) throws IOException, InterruptedException {
        for (FilePath path : root) {
            if (path.isDirectory()) {
                findUFTTestsPath(path.list(), testData);
            } else {
                if (path.getName().contains(".tsp")) {
                    String convertResourceMtrAsJSON = UFTParameterFactory.convertResourceMtrAsJSON(path.getParent().child("Action0").child("Resource.mtr").read());
                    testData.put(path.getParent().getName(), convertResourceMtrAsJSON);
                }
            }
        }
    }

    private static <T> T getExtension(Class<T> clazz) {
        ExtensionList<T> items = Jenkins.getInstance().getExtensionList(clazz);
        return items.get(0);
    }

    private MqmRestClient createClient() {
        ServerConfiguration configuration = ConfigurationService.getServerConfiguration();
        JenkinsMqmRestClientFactory clientFactory = getExtension(JenkinsMqmRestClientFactory.class);
        MqmRestClient client = clientFactory.obtain(
                configuration.location,
                configuration.sharedSpace,
                configuration.username,
                configuration.password);
        return client;
    }

    UFTTestDetectionBuildAction(final String message, final AbstractBuild<?, ?> build, String workspaceId) {
        this.message = message;
        this.build = build;
        MqmRestClient client = createClient();
        ServerConfiguration serverConfiguration = ConfigurationService.getServerConfiguration();
        try {
            HashMap<String, String> uftTestData = new HashMap<>();
            findUFTTestsPath(build.getWorkspace().list(), uftTestData);
            ArrayList<AutomatedTest> data = new ArrayList<>();
            AutomatedTests automatedTests = new AutomatedTests();

            logger.info(uftTestData.toString());

            Set<String> keys = uftTestData.keySet();
            String[] uftTestNames = keys.toArray(new String[keys.size()]);
            for (int i = 0; i < uftTestNames.length; i++) {
                String uftTestName = uftTestNames[i];
                AutomatedTest automatedTest = new AutomatedTest();
                // todo: To enable once decided, need to get the ID dynamicly from server.
//                automatedTest.setFramework(new TestFramework());
//                automatedTest.setTesting_tool_type(new com.hp.octane.plugins.jenkins.actions.dto.TestingToolType());
                automatedTest.setName(uftTestName);
                data.add(automatedTest);
            }
            automatedTests.setData(data);
            String uftTestJson = JSONObject.fromObject(automatedTests).toString();
            String serverURL = getServerURL(workspaceId, serverConfiguration.sharedSpace, serverConfiguration.location);
            JSONObject jsonObject = client.postTest(uftTestJson, uftTestData, serverURL);
            for (int i = 0; i < jsonObject.getInt("total_count"); i++) {
                String testID = ((JSONObject) jsonObject.getJSONArray("data").get(i)).getString("id");
                String testName = ((JSONObject) jsonObject.getJSONArray("data").get(i)).getString("name");
                try {
                    String parametersJSON = uftTestData.get(testName);
                    if (parametersJSON != null) {
                        client.attachUFTParametersToTest(testID, parametersJSON, serverURL);
                    }

                } catch (IOException e) {
                    logger.severe(e.getMessage());
                }
            }
        } catch (InterruptedException | IOException e) {
            logger.severe(e.getMessage());
        }
    }

    private String getServerURL(String workspaceId, String sharedspaceId, String location) {
        return location + "/api/shared_spaces/" + sharedspaceId + "/workspaces/" + workspaceId;
    }
}