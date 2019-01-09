/*
 *
 *  Certain versions of software and/or documents (“Material”) accessible here may contain branding from
 *  Hewlett-Packard Company (now HP Inc.) and Hewlett Packard Enterprise Company.  As of September 1, 2017,
 *  the Material is now offered by Micro Focus, a separately owned and operated company.  Any reference to the HP
 *  and Hewlett Packard Enterprise/HPE marks is historical in nature, and the HP and Hewlett Packard Enterprise/HPE
 *  marks are the property of their respective owners.
 * __________________________________________________________________
 * MIT License
 *
 * © Copyright 2012-2018 Micro Focus or one of its affiliates.
 *
 * The only warranties for products and services of Micro Focus and its affiliates
 * and licensors (“Micro Focus”) are set forth in the express warranty statements
 * accompanying such products and services. Nothing herein should be construed as
 * constituting an additional warranty. Micro Focus shall not be liable for technical
 * or editorial errors or omissions contained herein.
 * The information contained herein is subject to change without notice.
 * ___________________________________________________________________
 *
 */

package com.microfocus.application.automation.tools.octane.executor;

import com.hp.octane.integrations.OctaneClient;
import com.hp.octane.integrations.OctaneSDK;
import com.hp.octane.integrations.dto.entities.Entity;
import com.hp.octane.integrations.dto.entities.EntityConstants;
import com.hp.octane.integrations.services.entities.EntitiesService;
import com.hp.octane.integrations.services.entities.QueryHelper;
import com.microfocus.application.automation.tools.octane.actions.UFTTestDetectionPublisher;
import com.microfocus.application.automation.tools.octane.tests.AbstractSafeLoggingAsyncPeriodWork;
import hudson.Extension;
import hudson.model.FreeStyleProject;
import hudson.model.PeriodicWork;
import hudson.model.TaskListener;
import jenkins.model.Jenkins;
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

    public UftJobCleaner() {
        super("Uft Job Cleaner");
        logger.warn(String.format("Initial delay %d minutes, recurrencePeriod %d minutes, outdate threshold %d days", getInitialDelay() / MIN, getRecurrencePeriod() / MIN, getOutdateThreshold()));
    }

    @Override
    public long getRecurrencePeriod() {
        return DAY;
    }

    @Override
    public long getInitialDelay() {
        return MIN * 10;//start 10 minutes after jenkins restart
    }

    private long getOutdateThreshold() {
        int DEFAULT_OUTDATE_THRESHOLD = 30;
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
            if (UftJobRecognizer.isExecutorJob(job) && job.getLastBuild() != null && !job.isBuilding()) {
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

    private void clearDiscoveryJobs(List<FreeStyleProject> jobs) {

        //Generally, after deleting executor in Octane, relevant job in Jenkins is also deleted. But if jenkins was down during delete of executor, job remains
        //This method handle orphan job that doesn't have matching executors in Octane
        for (FreeStyleProject proj : jobs) {
            if (UftJobRecognizer.isDiscoveryJob(proj)) {
                try {
                    String executorLogicalName = UftJobRecognizer.getExecutorLogicalName(proj);
                    if (StringUtils.isNotEmpty(executorLogicalName)) {
                        Long workspaceId = getOctaneWorkspaceIdInDiscoveryJob(proj);
                        String configurationId = getConfigurationIdInDiscoveryJob(proj);
                        OctaneClient octaneClient = OctaneSDK.getClientByInstanceId(configurationId);
                        EntitiesService entitiesService = octaneClient.getEntitiesService();
                        List<String> conditions = Arrays.asList(QueryHelper.condition(EntityConstants.Base.LOGICAL_NAME_FIELD, executorLogicalName));
                        List<Entity> executors = entitiesService.getEntities(workspaceId, EntityConstants.Executors.COLLECTION_NAME, conditions, null);
                        if (executors.isEmpty()) {
                            try {
                                logger.warn(String.format("Job %s is going to be deleted as is doesn't have matching executor in Octane in workspace %s", proj.getName(), workspaceId));
                                proj.delete();
                            } catch (Exception e) {
                                logger.warn(String.format("Failed to delete job %s : %s", proj.getName(), e.getMessage()));
                            }

                            deleteExecutionJobByExecutorIfNeverExecuted(executorLogicalName);
                        }
                    }
                } catch (Exception e) {
                    logger.warn(String.format("Failed to clearDiscoveryJobs %s : %s", proj.getName(), e.getMessage()));
                }
            }
        }
    }

    private Long getOctaneWorkspaceIdInDiscoveryJob(FreeStyleProject job) {
        UFTTestDetectionPublisher uftTestDetectionPublisher;
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

    private String getConfigurationIdInDiscoveryJob(FreeStyleProject job) {
        UFTTestDetectionPublisher uftTestDetectionPublisher;
        List publishers = job.getPublishersList();
        for (Object publisher : publishers) {
            if (publisher instanceof UFTTestDetectionPublisher) {
                uftTestDetectionPublisher = (UFTTestDetectionPublisher) publisher;
                String configurationId = uftTestDetectionPublisher.getConfigurationId();
                return configurationId;
            }
        }
        return null;
    }

    public static void deleteExecutionJobByExecutorIfNeverExecuted(String executorToDelete) {
        List<FreeStyleProject> jobs = Jenkins.getInstance().getAllItems(FreeStyleProject.class);
        for (FreeStyleProject proj : jobs) {
            if (UftJobRecognizer.isExecutorJob(proj)) {
                String executorId = UftJobRecognizer.getExecutorId(proj);
                String executorLogicalName = UftJobRecognizer.getExecutorLogicalName(proj);
                if ((StringUtils.isNotEmpty(executorId) && executorId.equals(executorToDelete)) ||
                        (StringUtils.isNotEmpty(executorLogicalName) && executorLogicalName.equals(executorToDelete))) {
                    if (proj.getLastBuild() == null && !proj.isBuilding() && !proj.isInQueue()) {
                        try {
                            logger.warn(String.format("Job '%s' is going to be deleted since matching executor in Octane was deleted and this job was never executed and has no history.", proj.getName()));
                            proj.delete();
                        } catch (IOException | InterruptedException e) {
                            logger.error("Failed to delete job  " + proj.getName() + " : " + e.getMessage());
                        }
                    }
                }
            }
        }
    }

    /**
     * Delete discovery job that related to specific executor in Octane
     *
     * @param executorToDelete
     */
    public static void deleteDiscoveryJobByExecutor(String executorToDelete) {

        List<FreeStyleProject> jobs = Jenkins.getInstance().getAllItems(FreeStyleProject.class);
        for (FreeStyleProject proj : jobs) {
            if (UftJobRecognizer.isDiscoveryJob(proj)) {
                String executorId = UftJobRecognizer.getExecutorId(proj);
                String executorLogicalName = UftJobRecognizer.getExecutorLogicalName(proj);
                if ((StringUtils.isNotEmpty(executorId) && executorId.equals(executorToDelete)) ||
                        (StringUtils.isNotEmpty(executorLogicalName) && executorLogicalName.equals(executorToDelete))) {
                    boolean waitBeforeDelete = false;

                    if (proj.isBuilding()) {
                        proj.getLastBuild().getExecutor().interrupt();
                        waitBeforeDelete = true;
                    } else if (proj.isInQueue()) {
                        Jenkins.getInstance().getQueue().cancel(proj);
                        waitBeforeDelete = true;
                    }

                    if (waitBeforeDelete) {
                        try {
                            //we cancelled building/queue - wait before deleting the job, so Jenkins will be able to complete some IO actions
                            Thread.sleep(10000);
                        } catch (InterruptedException e) {
                            //do nothing
                        }
                    }

                    try {
                        logger.warn(String.format("Job '%s' is going to be deleted since matching executor in Octane was deleted", proj.getName()));
                        proj.delete();
                    } catch (IOException | InterruptedException e) {
                        logger.error("Failed to delete job  " + proj.getName() + " : " + e.getMessage());
                    }
                }
            }
        }
    }
}
