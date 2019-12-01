package com.microfocus.application.automation.tools.octane.vulnerabilities;

import com.hp.octane.integrations.OctaneSDK;
import com.hp.octane.integrations.services.vulnerabilities.ToolType;
import com.microfocus.application.automation.tools.model.OctaneServerSettingsModel;
import com.microfocus.application.automation.tools.octane.configuration.ConfigurationService;
import com.microfocus.application.automation.tools.octane.tests.build.BuildHandlerUtils;
import hudson.model.Run;

import java.util.HashMap;
import java.util.Map;

public class VulnerabilitiesUtils {

    public static void insertFODQueueItem(Run run, Long releaseId ) {
        HashMap<String,String> additionalProperties = new HashMap<>();
        additionalProperties.put("releaseId", releaseId.toString());
        insertQueueItem(run, ToolType.FOD, additionalProperties);
    }

    public static void insertQueueItem(Run run, ToolType toolType, Map<String,String> props) {
        String jobCiId = BuildHandlerUtils.getJobCiId(run);
        String buildCiId = BuildHandlerUtils.getBuildCiId(run);

        //  [YG]: TODO productize the below code to be able to override the global maxTimeoutHours by Job's own configuration
//		long queueItemTimeout = 0;
//		ParametersAction parameters = run.getAction(ParametersAction.class);
//		if (parameters != null && parameters.getParameter("some-predefined-value") != null) {
//			queueItemTimeout = Long.parseLong((String) parameters.getParameter("some-predefined-value").getValue());
//		}

        OctaneSDK.getClients().forEach(octaneClient -> {
            String instanceId = octaneClient.getInstanceId();
            OctaneServerSettingsModel settings = ConfigurationService.getSettings(instanceId);
            if (settings != null && !settings.isSuspend()) {
                octaneClient.getVulnerabilitiesService().enqueueRetrieveAndPushVulnerabilities(
                        jobCiId,
                        buildCiId, toolType,
                        run.getStartTimeInMillis(),
                        settings.getMaxTimeoutHours(),
                        props);
            }
        });
    }
}
