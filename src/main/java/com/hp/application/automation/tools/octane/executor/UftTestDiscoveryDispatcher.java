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

package com.hp.application.automation.tools.octane.executor;

import com.google.inject.Inject;
import com.hp.application.automation.tools.common.HttpStatus;
import com.hp.application.automation.tools.octane.ResultQueue;
import com.hp.application.automation.tools.octane.actions.UftTestType;
import com.hp.application.automation.tools.octane.actions.dto.*;
import com.hp.application.automation.tools.octane.client.JenkinsMqmRestClientFactory;
import com.hp.application.automation.tools.octane.client.JenkinsMqmRestClientFactoryImpl;
import com.hp.application.automation.tools.octane.configuration.ConfigurationService;
import com.hp.application.automation.tools.octane.configuration.ServerConfiguration;
import com.hp.application.automation.tools.octane.tests.AbstractSafeLoggingAsyncPeriodWork;
import com.hp.mqm.client.MqmRestClient;
import com.hp.mqm.client.exception.LoginException;
import com.hp.mqm.client.exception.RequestErrorException;
import com.hp.mqm.client.exception.RequestException;
import com.hp.mqm.client.exception.SharedSpaceNotExistException;
import com.hp.mqm.client.model.Entity;
import com.hp.mqm.client.model.ListItem;
import com.hp.mqm.client.model.PagedList;
import hudson.Extension;
import hudson.model.AbstractBuild;
import hudson.model.Job;
import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.util.TimeUnit2;
import jenkins.model.Jenkins;
import net.sf.json.JSONObject;
import net.sf.json.JsonConfig;
import net.sf.json.processors.PropertyNameProcessor;
import net.sf.json.util.PropertyFilter;
import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.*;


/**
 * This class is responsible to send discovered uft tests to Octane.
 * Class uses file-based queue so if octane or jenkins will be down before sending,
 * after connection is up - this dispatcher will send tests to Octane.
 * <p>
 * Actually list of discovered tests are persisted in job run directory. Queue contains only reference to that job run.
 */
@Extension
public class UftTestDiscoveryDispatcher extends AbstractSafeLoggingAsyncPeriodWork {

    private static Logger logger = LogManager.getLogger(UftTestDiscoveryDispatcher.class);
    private final static String TESTS_COLLECTION_NAME = "tests";
    private final static String SCM_RESOURCE_COLLECTION_NAME = "scm_resources";

    private final static int BULK_SIZE = 100;

    private UftTestDiscoveryQueue queue;
    private JenkinsMqmRestClientFactory clientFactory;

    public UftTestDiscoveryDispatcher() {
        super("Uft Test Discovery Dispatcher");
    }


    @Override
    protected void doExecute(TaskListener listener) throws IOException, InterruptedException {
        if (queue.peekFirst() == null) {
            return;
        }

        logger.warn("Queue size  " + queue.size());
        //logger.info("... done, left to send " + events.size() + " events");
        ServerConfiguration serverConfiguration = ConfigurationService.getServerConfiguration();
        MqmRestClient client = createClient(serverConfiguration);
        if (client == null) {
            return;
        }

        ResultQueue.QueueItem item;
        while ((item = queue.peekFirst()) != null) {

            Job project = (Job) Jenkins.getInstance().getItemByFullName(item.getProjectName());
            if (project == null) {
                logger.warn("Project [" + item.getProjectName() + "] no longer exists, pending discovered tests can't be submitted");
                queue.remove();
                continue;
            }

            Run build = project.getBuildByNumber(item.getBuildNumber());
            if (build == null) {
                logger.warn("Build [" + item.getProjectName() + "#" + item.getBuildNumber() + "] no longer exists, pending discovered tests can't be submitted");
                queue.remove();
                continue;
            }

            UFTTestDetectionResult result = UFTTestDetectionService.readDetectionResults(build);
            if (result == null) {
                logger.warn("Build [" + item.getProjectName() + "#" + item.getBuildNumber() + "] no longer contains valid detection result file");
                queue.remove();
                continue;
            }

            logger.warn("Persistence [" + item.getProjectName() + "#" + item.getBuildNumber() + "]");
            dispatchDetectionResults(item, client, result);
            if (result.isInitialDetection()) {
                UFTTestDetectionService.createInitialDetectionFile(((AbstractBuild) build).getWorkspace());
            }
            queue.remove();
        }
    }

    private void dispatchDetectionResults(ResultQueue.QueueItem item, MqmRestClient client, UFTTestDetectionResult result) throws UnsupportedEncodingException {
        //post new tests
        if (!result.getNewTests().isEmpty()) {
            boolean posted = postTests(client, result.getNewTests(), result.getWorkspaceId(), result.getScmResourceId());
            logger.warn("Persistence [" + item.getProjectName() + "#" + item.getBuildNumber() + "] : " + result.getNewTests().size() + "  new tests posted successfully = " + posted);
        }

        //post scm resources
        if (!result.getNewScmResourceFiles().isEmpty()) {
            boolean posted = postScmResources(client, result.getNewScmResourceFiles(), result.getWorkspaceId(), result.getScmResourceId());
            logger.warn("Persistence [" + item.getProjectName() + "#" + item.getBuildNumber() + "] : " + result.getNewScmResourceFiles().size() + "  new scmResources posted successfully = " + posted);
        }

        //post updated
        if (!result.getUpdatedTests().isEmpty()) {
            boolean updated = updateTests(client, result.getUpdatedTests(), result.getWorkspaceId());
            logger.warn("Persistence [" + item.getProjectName() + "#" + item.getBuildNumber() + "] : " + result.getUpdatedTests().size() + "  updated tests posted successfully = " + updated);
        }
    }

    private static boolean postTests(MqmRestClient client, List<AutomatedTest> tests, String workspaceId, String scmResourceId) throws UnsupportedEncodingException {

        if (!tests.isEmpty()) {
            try {
                completeTestProperties(client, Long.parseLong(workspaceId), tests, scmResourceId);
            } catch (RequestErrorException e) {
                logger.error("Failed to completeTestProperties : " + e.getMessage());
                return false;
            }

            for (int i = 0; i < tests.size(); i += BULK_SIZE)
                try {
                    AutomatedTests data = AutomatedTests.createWithTests(tests.subList(i, Math.min(i + BULK_SIZE, tests.size())));
                    String uftTestJson = convertToJsonString(data);

                    client.postEntities(Long.parseLong(workspaceId), TESTS_COLLECTION_NAME, uftTestJson);

                } catch (RequestErrorException e) {
                    if (e.getStatusCode() != HttpStatus.CONFLICT.getCode()) {
                        logger.error("Failed to postTests to Octane : " + e.getMessage());
                        return false;
                    }

                    //else :  the test with the same hash code , so do nothing
                }
        }
        return true;
    }

    private static boolean postScmResources(MqmRestClient client, List<ScmResourceFile> resources, String workspaceId, String scmResourceId) throws UnsupportedEncodingException {

        if (!resources.isEmpty()) {
            try {
                completeScmResourceProperties(client, Long.parseLong(workspaceId), resources, scmResourceId);
            } catch (RequestErrorException e) {
                logger.error("Failed to completeTestProperties : " + e.getMessage());
                return false;
            }

            for (int i = 0; i < resources.size(); i += BULK_SIZE)
                try {
                    ScmResources data = ScmResources.createWithItems(resources.subList(i, Math.min(i + BULK_SIZE, resources.size())));
                    String uftTestJson = convertToJsonString(data);

                    client.postEntities(Long.parseLong(workspaceId), SCM_RESOURCE_COLLECTION_NAME, uftTestJson);

                } catch (RequestErrorException e) {
                    if (e.getStatusCode() != HttpStatus.CONFLICT.getCode()) {
                        logger.error("Failed to postTests to Octane : " + e.getMessage());
                        return false;
                    }

                    //else :  the test with the same hash code , so do nothing
                }
        }
        return true;
    }

    private static String convertToJsonString(Object data) {
        JsonConfig config = getJsonConfig();
        return JSONObject.fromObject(data, config).toString();
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

        config.registerJsonPropertyNameProcessor(ScmResourceFile.class, new PropertyNameProcessor() {

            @Override
            public String processPropertyName(Class className, String fieldName) {
                String result = fieldName;
                switch (fieldName) {
                    case "relativePath":
                        result = "relative_path";
                        break;
                    case "scmRepository":
                        result = "scm_repository";
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
                PagedList<Entity> foundTests = client.getEntities(workspaceIdAsLong, TESTS_COLLECTION_NAME, queryFields, Arrays.asList("id, description"));
                if (foundTests.getItems().size() == 1) {
                    Entity foundTest = foundTests.getItems().get(0);
                    AutomatedTest testForUpdate = new AutomatedTest();
                    testForUpdate.setSubtype(null);
                    testForUpdate.setDescription(test.getDescription());
                    testForUpdate.setId(foundTest.getId());
                    String json = convertToJsonString(testForUpdate);
                    client.updateEntity(Long.parseLong(workspaceId), TESTS_COLLECTION_NAME, foundTests.getItems().get(0).getId(), json);
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
            if (UftTestType.API.equals(test.getUftTestType())) {
                testType = apiTestType;
            }
            test.setTestTypes(ListNodeEntityCollection.create(testType));
        }
    }

    private static void completeScmResourceProperties(MqmRestClient client, long l, List<ScmResourceFile> resources, String scmResourceId) {
        BaseRefEntity scmRepository = StringUtils.isEmpty(scmResourceId) ? null : BaseRefEntity.create("scm_repository", Long.valueOf(scmResourceId));
        for (ScmResourceFile resource : resources) {
            resource.setScmRepository(scmRepository);
        }
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

    private MqmRestClient createClient(ServerConfiguration serverConfiguration) {

        if (!serverConfiguration.isValid()) {
            logger.warn("There are pending discovered UFT tests, but MQM server configuration is not valid, results can't be submitted");
            return null;
        }

        MqmRestClient client = clientFactory.obtain(
                serverConfiguration.location,
                serverConfiguration.sharedSpace,
                serverConfiguration.username,
                serverConfiguration.password);

        try {
            client.validateConfigurationWithoutLogin();
            return client;
        } catch (SharedSpaceNotExistException e) {
            logger.warn("Invalid shared space");
        } catch (LoginException e) {
            logger.warn("Login failed : " + e.getMessage());
        } catch (RequestException e) {
            logger.warn("Problem with communication with MQM server : " + e.getMessage());
        }

        return null;
    }

    @Override
    public long getRecurrencePeriod() {
        String value = System.getProperty("UftTestDiscoveryDispatcher.Period"); // let's us config the recurrence period. default is 60 seconds.
        if (!StringUtils.isEmpty(value)) {
            return Long.valueOf(value);
        }
        return TimeUnit2.SECONDS.toMillis(30);
    }

    @Inject
    public void setTestResultQueue(UftTestDiscoveryQueue queue) {
        this.queue = queue;
    }

    @Inject
    public void setMqmRestClientFactory(JenkinsMqmRestClientFactoryImpl clientFactory) {
        this.clientFactory = clientFactory;
    }

    private static boolean hasTestsByType(Collection<AutomatedTest> tests, UftTestType uftTestType) {
        for (AutomatedTest test : tests) {
            if (uftTestType.equals(test.getUftTestType())) {
                return true;
            }
        }
        return false;
    }

    /**
     * Queue that current run contains discovered tests
     *
     * @param projectName
     * @param buildNumber
     */
    public void enqueueResult(String projectName, int buildNumber) {
        queue.add(projectName, buildNumber);
    }
}
