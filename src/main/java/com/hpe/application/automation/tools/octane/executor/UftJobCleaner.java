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

package com.hpe.application.automation.tools.octane.executor;

import com.google.inject.Inject;
import com.hpe.application.automation.tools.octane.actions.UFTTestDetectionPublisher;
import com.hpe.application.automation.tools.octane.configuration.ConfigurationService;
import com.hpe.application.automation.tools.octane.configuration.ServerConfiguration;
import com.hpe.application.automation.tools.octane.tests.AbstractSafeLoggingAsyncPeriodWork;
import com.hp.mqm.client.MqmRestClient;
import com.hp.mqm.client.QueryHelper;
import com.hp.mqm.client.model.Entity;
import com.hp.mqm.client.model.PagedList;
import hudson.Extension;
import hudson.model.*;
import jenkins.model.Jenkins;
import org.apache.commons.collections.map.HashedMap;
import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.util.*;


/**
 * This class cleans outdated test execution jobs that were not used more than 7 days.
 * Only jobs that were created by {@link TestExecutionJobCreatorService} are checked.
 */
@Extension
public class UftJobCleaner extends AbstractSafeLoggingAsyncPeriodWork {

    private static Logger logger = LogManager.getLogger(UftJobCleaner.class);
    private ConfigurationService configurationService;
    private static String EXECUTORS_COLLECTION_NAME = "executors";

    public UftJobCleaner() {
        super("Uft Job Cleaner");
        logger.warn(String.format("Initial delay %d minutes, recurrencePeriod %d minutes, outdate threshold %d days", getInitialDelay() / MIN, getRecurrencePeriod() / MIN, getOutdateThreshold()));
    }

    @Override
    public long getRecurrencePeriod() {
        return HOUR;
    }

    @Override
    public long getInitialDelay() {
        return MIN * 10;//start 10 minutes after jenkins restart
    }

    private long getOutdateThreshold() {
        int DEFAULT_OUTDATE_THRESHOLD = 7;
        int threshold = DEFAULT_OUTDATE_THRESHOLD;
        String paramValue = System.getProperty("octane.plugin.UftJobCleaner.outdateThreshold");
        if (StringUtils.isNotEmpty(paramValue)) {
            try {
                threshold = Integer.parseInt(paramValue);
            } catch (NumberFormatException e) {
                threshold = DEFAULT_OUTDATE_THRESHOLD;
            }
        }
        return threshold;
    }

    @Override
    protected void doExecute(TaskListener listener) throws IOException, InterruptedException {
        List<FreeStyleProject> jobs = Jenkins.getInstance().getAllItems(FreeStyleProject.class);

        clearExecutionJobs(jobs);
        clearDiscoveryJobs(jobs);
    }

    private void clearExecutionJobs(List<FreeStyleProject> jobs) {
        long thresholdTimeInMillis = new Date().getTime() - PeriodicWork.DAY * getOutdateThreshold();
        int clearCounter = 0;
        for (FreeStyleProject job : jobs) {
            if (isExecutorJob(job) && job.getLastBuild() != null && !job.isBuilding()) {
                if (thresholdTimeInMillis > job.getLastBuild().getTimeInMillis()) {
                    try {
                        logger.warn(String.format("Job %s is going to be deleted as outdated job, last build was executed at %s", job.getName(), job.getLastBuild().getTimestampString2()));
                        job.delete();
                    } catch (Exception e) {
                        logger.warn(String.format("Failed to delete job %s : %s", job.getName(), e.getMessage()));
                    }

                    clearCounter++;
                }
            }
        }

        logger.warn(String.format("Cleaner found %s outdated execution job", clearCounter));
    }

    private boolean isExecutorJob(FreeStyleProject job) {
        ParametersDefinitionProperty parameters = job.getProperty(ParametersDefinitionProperty.class);
        boolean isExecutorJob = job.getName().contains("execution job") &&
                parameters != null &&
                parameters.getParameterDefinition(TestExecutionJobCreatorService.SUITE_ID_PARAMETER_NAME) != null &&
                parameters.getParameterDefinition(TestExecutionJobCreatorService.SUITE_RUN_ID_PARAMETER_NAME) != null;

        return isExecutorJob;
    }


    private boolean isDiscoveryJobJob(FreeStyleProject job) {
        ParametersDefinitionProperty parameters = job.getProperty(ParametersDefinitionProperty.class);
        boolean isDiscoveryJob = job.getName().contains("discovery job") &&
                parameters != null &&
                parameters.getParameterDefinition(TestExecutionJobCreatorService.EXECUTOR_ID_PARAMETER_NAME) != null;
        return isDiscoveryJob;
    }

    private void clearDiscoveryJobs(List<FreeStyleProject> jobs) {

        //Generally, after deleting executor in Octane, relevant job in Jenkins is also deleted. But if jenkins was down during delete of executor, job remains
        //This method handle orphan job that doesn't hava mathing executors in Octane
        //1. build map of discovery jobs per workspace
        //2. Loop by workspace
        //2.1 Get from octane executors in workspace
        //2.2 If some discovery job exist that doesn't have matching executor - remove it from Jenkins

        Map<Long, Map<Long, FreeStyleProject>> workspace2executorId2DiscoveryJobMap = new HashMap<>();
        for (FreeStyleProject job : jobs) {
            if (isDiscoveryJobJob(job)) {
                Long executorId = getExecutorId(job);
                Long workspaceId = getOctaneWorkspaceId(job);
                if (executorId != null && workspaceId != null) {
                    if (!workspace2executorId2DiscoveryJobMap.containsKey(workspaceId)) {
                        workspace2executorId2DiscoveryJobMap.put(workspaceId, new HashedMap());
                    }
                    workspace2executorId2DiscoveryJobMap.get(workspaceId).put(executorId, job);
                }
            }
        }

        if (!workspace2executorId2DiscoveryJobMap.isEmpty()) {
            ServerConfiguration serverConfiguration = ConfigurationService.getServerConfiguration();
            MqmRestClient client = configurationService.createClient(serverConfiguration);
            if (client != null) {
                int deleteCounter = 0;
                for (Long workspaceId : workspace2executorId2DiscoveryJobMap.keySet()) {
                    Map<Long, FreeStyleProject> discoveryJobs = workspace2executorId2DiscoveryJobMap.get(workspaceId);
                    List<String> conditions = new ArrayList<>();
                    conditions.add(QueryHelper.conditionIn("id", discoveryJobs.keySet(), true));
                    try {
                        PagedList<Entity> entities = client.getEntities(workspaceId, EXECUTORS_COLLECTION_NAME, conditions, Arrays.asList("id"));
                        Set<Long> octaneExecutorIds = new HashSet<>();
                        for (Entity executor : entities.getItems()) {
                            octaneExecutorIds.add(executor.getId());
                        }
                        for (Long jobExecutorId : discoveryJobs.keySet()) {
                            if (!octaneExecutorIds.contains(jobExecutorId)) {
                                //found discovery job that is not related to any executor in Octane
                                FreeStyleProject job = discoveryJobs.get(jobExecutorId);
                                try {
                                    logger.warn(String.format("Job %s is going to be deleted as is doesn't have matching executor in Octane in workspace %s", job.getName(), workspaceId));
                                    deleteCounter++;
                                    job.delete();

                                } catch (Exception e) {
                                    logger.warn(String.format("Failed to delete job %s : %s", job.getName(), e.getMessage()));
                                }

                            }
                        }
                    } catch (Exception e) {
                        //on exception - do nothing and skip to next workspace
                        logger.warn(String.format("Failed to get executors from workspace %s : %s", workspaceId, e.getMessage()));
                        continue;
                    }
                }
                logger.warn(String.format("Cleaner found %s not-related discovery job", deleteCounter));
            }
        }
    }


    private Long getExecutorId(FreeStyleProject job) {
        ParametersDefinitionProperty parameters = job.getProperty(ParametersDefinitionProperty.class);
        ParameterDefinition pd = parameters.getParameterDefinition(TestExecutionJobCreatorService.EXECUTOR_ID_PARAMETER_NAME);
        String value = (String) pd.getDefaultParameterValue().getValue();
        return Long.valueOf(value);
    }

    private Long getOctaneWorkspaceId(FreeStyleProject job) {

        UFTTestDetectionPublisher uftTestDetectionPublisher = null;
        List publishers = job.getPublishersList();
        for (Object publisher : publishers) {
            if (publisher instanceof UFTTestDetectionPublisher) {
                uftTestDetectionPublisher = (UFTTestDetectionPublisher) publisher;
                String workspaceId = uftTestDetectionPublisher.getWorkspaceName();
                return Long.valueOf(workspaceId);
            }
        }

        return null;
    }

    @Inject
    public void setConfigurationService(ConfigurationService configurationService) {
        this.configurationService = configurationService;
    }

}
