/*
 * Certain versions of software and/or documents ("Material") accessible here may contain branding from
 * Hewlett-Packard Company (now HP Inc.) and Hewlett Packard Enterprise Company.  As of September 1, 2017,
 * the Material is now offered by Micro Focus, a separately owned and operated company.  Any reference to the HP
 * and Hewlett Packard Enterprise/HPE marks is historical in nature, and the HP and Hewlett Packard Enterprise/HPE
 * marks are the property of their respective owners.
 * __________________________________________________________________
 * MIT License
 *
 * (c) Copyright 2012-2021 Micro Focus or one of its affiliates.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated
 * documentation files (the "Software"), to deal in the Software without restriction, including without limitation
 * the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software,
 * and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or
 * substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO
 * THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT,
 * TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 *
 * ___________________________________________________________________
 */

package com.microfocus.application.automation.tools.octane.executor;

import com.hp.octane.integrations.utils.CIPluginSDKUtils;
import com.microfocus.application.automation.tools.octane.configuration.SDKBasedLoggerProvider;
import hudson.model.FreeStyleProject;
import hudson.model.ParameterDefinition;
import hudson.model.ParametersDefinitionProperty;
import jenkins.model.Jenkins;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.util.List;


/***
 * Recognizer for UFT related jobs
 */
public class UftJobRecognizer {

    private static Logger logger = SDKBasedLoggerProvider.getLogger(UftJobRecognizer.class);

    /**
     * Check if current job is EXECUTOR job
     *
     * @param job
     * @return
     */
    public static boolean isExecutorJob(FreeStyleProject job) {
        return (job.getName().startsWith(UftConstants.EXECUTION_JOB_MIDDLE_NAME_WITH_TEST_RUNNERS) ||
                job.getName().startsWith(UftConstants.EXECUTION_JOB_MIDDLE_NAME_WITH_TEST_RUNNERS_NEW));
    }

    /**
     * Check if current job is discovery job
     *
     * @param job
     * @return
     */
    public static boolean isDiscoveryJob(FreeStyleProject job) {
        return (job.getName().startsWith(UftConstants.DISCOVERY_JOB_MIDDLE_NAME_WITH_TEST_RUNNERS) ||
                job.getName().startsWith(UftConstants.DISCOVERY_JOB_MIDDLE_NAME_WITH_TEST_RUNNERS_NEW));
    }

    /**
     * Extract executor id from the job
     *
     * @param job
     * @return
     */
    public static String getExecutorId(FreeStyleProject job) {
        ParametersDefinitionProperty parameters = job.getProperty(ParametersDefinitionProperty.class);
        ParameterDefinition pd = parameters.getParameterDefinition(UftConstants.TEST_RUNNER_ID_PARAMETER_NAME);
        if (pd != null) {
            return (String) pd.getDefaultParameterValue().getValue();
        } else {
            return null;
        }
    }

    /**
     * Extract executor logical name from the job
     *
     * @param job
     * @return
     */
    public static String getExecutorLogicalName(FreeStyleProject job) {
        ParametersDefinitionProperty parameters = job.getProperty(ParametersDefinitionProperty.class);
        ParameterDefinition pd = parameters.getParameterDefinition(UftConstants.TEST_RUNNER_LOGICAL_NAME_PARAMETER_NAME);
        if (pd != null) {
            return (String) pd.getDefaultParameterValue().getValue();
        } else {
            return null;
        }
    }

    public static void deleteExecutionJobByExecutorIfNeverExecuted(String executorToDelete) {
        List<FreeStyleProject> jobs = Jenkins.getInstanceOrNull().getAllItems(FreeStyleProject.class);
        for (FreeStyleProject proj : jobs) {
            if (UftJobRecognizer.isExecutorJob(proj) && isJobMatchExecutor(executorToDelete, proj)
                    && proj.getLastBuild() == null && !proj.isBuilding() && !proj.isInQueue()) {
                try {
                    logger.warn(String.format("Job '%s' is going to be deleted since matching executor in Octane was deleted and this job was never executed and has no history.", proj.getName()));
                    proj.delete();
                } catch (IOException | InterruptedException e) {
                    logger.error("Failed to delete job  " + proj.getName() + " : " + e.getMessage());
                }
            }
        }
    }

    private static Boolean isJobMatchExecutor(String executorToFind, FreeStyleProject proj) {
        String executorId = UftJobRecognizer.getExecutorId(proj);
        String executorLogicalName = UftJobRecognizer.getExecutorLogicalName(proj);
        return (executorId != null && executorId.equals(executorToFind)) ||
                (executorLogicalName != null && executorLogicalName.equals(executorToFind));
    }

    /**
     * Delete discovery job that related to specific executor in Octane
     *
     * @param executorToDelete
     */
    public static void deleteDiscoveryJobByExecutor(String executorToDelete) {

        List<FreeStyleProject> jobs = Jenkins.getInstanceOrNull().getAllItems(FreeStyleProject.class);
        for (FreeStyleProject proj : jobs) {
            if (UftJobRecognizer.isDiscoveryJob(proj) && isJobMatchExecutor(executorToDelete, proj)) {
                if (proj.isBuilding()) {
                    proj.getLastBuild().getExecutor().interrupt();
                    CIPluginSDKUtils.doWait(10000); //wait before deleting the job, so Jenkins will be able to complete some IO actions
                } else if (proj.isInQueue()) {
                    Jenkins.getInstanceOrNull().getQueue().cancel(proj);
                    CIPluginSDKUtils.doWait(10000); //wait before deleting the job, so Jenkins will be able to complete some IO actions
                }

                try {
                    logger.warn(String.format("Job '%s' is going to be deleted since matching executor in Octane was deleted", proj.getName()));
                    proj.delete();
                } catch (Exception e) {
                    logger.error("Failed to delete job  " + proj.getName() + " : " + e.getMessage());
                }
            }
        }
    }
}
