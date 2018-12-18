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

import hudson.model.FreeStyleProject;
import hudson.model.ParameterDefinition;
import hudson.model.ParametersDefinitionProperty;


/***
 * Recognizer for UFT related jobs
 */
public class UftJobRecognizer {

    /**
     * Check if current job is EXECUTOR job
     * @param job
     * @return
     */
    public static boolean isExecutorJob(FreeStyleProject job) {
        ParametersDefinitionProperty parameters = job.getProperty(ParametersDefinitionProperty.class);
        boolean isExecutorJob = job.getName().contains(UftConstants.EXECUTION_JOB_MIDDLE_NAME) &&
                parameters != null &&
                parameters.getParameterDefinition(UftConstants.SUITE_ID_PARAMETER_NAME) != null;

        return isExecutorJob;
    }

    /**
     * Check if current job is discovery job
     * @param job
     * @return
     */
    public static boolean isDiscoveryJob(FreeStyleProject job) {
        boolean isDiscoveryJob = (job.getName().contains(UftConstants.DISCOVERY_JOB_MIDDLE_NAME) ||
                job.getName().startsWith(UftConstants.DISCOVERY_JOB_MIDDLE_NAME_WITH_TEST_RUNNERS));
        return isDiscoveryJob;
    }

    /**
     * Extract executor id from the job
     * @param job
     * @return
     */
    public static String getExecutorId(FreeStyleProject job) {
        ParametersDefinitionProperty parameters = job.getProperty(ParametersDefinitionProperty.class);
        String parameterName = UftConstants.TEST_RUNNER_ID_PARAMETER_NAME;
        if (!parameters.getParameterDefinitionNames().contains(parameterName)) {
            parameterName = UftConstants.EXECUTOR_ID_PARAMETER_NAME;
        }
        ParameterDefinition pd = parameters.getParameterDefinition(parameterName);
        String value = (String) pd.getDefaultParameterValue().getValue();
        return value;
    }

    /**
     * Extract executor logical name from the job
     * @param job
     * @return
     */
    public static String getExecutorLogicalName(FreeStyleProject job) {
        ParametersDefinitionProperty parameters = job.getProperty(ParametersDefinitionProperty.class);
        String parameterName = UftConstants.EXECUTOR_LOGICAL_NAME_PARAMETER_NAME;
        if (!parameters.getParameterDefinitionNames().contains(parameterName)) {
            parameterName = UftConstants.TEST_RUNNER_LOGICAL_NAME_PARAMETER_NAME;
        }
        ParameterDefinition pd = parameters.getParameterDefinition(parameterName);
        String value = (String) pd.getDefaultParameterValue().getValue();
        return value;
    }
}
