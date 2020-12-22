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

package com.microfocus.application.automation.tools.octane.vulnerabilities;

import com.hp.octane.integrations.OctaneClient;
import com.hp.octane.integrations.OctaneSDK;
import com.hp.octane.integrations.services.configurationparameters.FortifySSCFetchTimeoutParameter;
import com.hp.octane.integrations.services.vulnerabilities.ToolType;
import com.microfocus.application.automation.tools.octane.configuration.SDKBasedLoggerProvider;
import com.microfocus.application.automation.tools.octane.configuration.SSCServerConfigUtil;
import com.microfocus.application.automation.tools.octane.tests.build.BuildHandlerUtils;
import hudson.model.ParametersAction;
import hudson.model.Run;
import org.apache.logging.log4j.Logger;

import java.util.HashMap;
import java.util.Map;

public class VulnerabilitiesUtils {
    private static Logger logger = SDKBasedLoggerProvider.getLogger(VulnerabilitiesUtils.class);
    private VulnerabilitiesUtils() {}

    public static void insertFODQueueItem(Run run, Long releaseId ) {
        HashMap<String,String> additionalProperties = new HashMap<>();
        additionalProperties.put("releaseId", releaseId.toString());
        insertQueueItem(run, ToolType.FOD, additionalProperties);
    }


    public static boolean insertQueueItem(Run run, SSCServerConfigUtil.SSCProjectVersionPair projectVersionPair) {
        if (projectVersionPair != null) {
            logger.warn("SSC configuration was found in " + run);
            String sscServerUrl = SSCServerConfigUtil.getSSCServer();
            if (sscServerUrl == null || sscServerUrl.isEmpty()) {
                logger.debug("SSC configuration not found in the whole CI Server");
                return false;
            }
            VulnerabilitiesUtils.insertQueueItem(run, ToolType.SSC, null);
        }
        return true;
    }

    private static void insertQueueItem(Run run, ToolType toolType, Map<String,String> props) {
        String jobCiId = BuildHandlerUtils.getJobCiId(run);
        String buildCiId = BuildHandlerUtils.getBuildCiId(run);

        final Long queueItemTimeoutHours = getQueueItemTimeoutHoursFromJob(run);
        String parents = BuildHandlerUtils.getRootJobCiIds(run);
        OctaneSDK.getClients().forEach(octaneClient -> {
            octaneClient.getVulnerabilitiesService().enqueueRetrieveAndPushVulnerabilities(
                    jobCiId,
                    buildCiId, toolType,
                    run.getStartTimeInMillis(),
                    queueItemTimeoutHours == null ? getFortifyTimeoutHours(octaneClient.getInstanceId()) : queueItemTimeoutHours,
                    props,
                    parents);
        });
    }

    public static int getFortifyTimeoutHours(String instanceId){
        OctaneClient octaneClient = OctaneSDK.getClientByInstanceId(instanceId);
        FortifySSCFetchTimeoutParameter parameter = (FortifySSCFetchTimeoutParameter) octaneClient.getConfigurationService().getConfiguration()
                .getParameter(FortifySSCFetchTimeoutParameter.KEY);
        if (parameter != null) {
            return parameter.getTimeout();
        }
        return FortifySSCFetchTimeoutParameter.DEFAULT_TIMEOUT;
    }

    private static Long getQueueItemTimeoutHoursFromJob(Run run) {
        Long queueItemTimeoutHours = null;
        String paramName = "fortify-maximum-analysis-timeout-hours";
        try {
            ParametersAction parameters = run.getAction(ParametersAction.class);
            if (parameters != null && parameters.getParameter(paramName) != null) {
                queueItemTimeoutHours = Long.parseLong((String) parameters.getParameter(paramName).getValue());
            }
        } catch (Exception e) {
            logger.warn("Failed to parse  " + paramName + " : " + e.getMessage());
            queueItemTimeoutHours = null;
        }
        return queueItemTimeoutHours;
    }

}
