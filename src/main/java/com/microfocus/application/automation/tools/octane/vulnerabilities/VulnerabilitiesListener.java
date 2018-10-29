/*
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
 */

package com.microfocus.application.automation.tools.octane.vulnerabilities;

import com.hp.octane.integrations.OctaneSDK;
import com.microfocus.application.automation.tools.model.OctaneServerSettingsModel;
import com.microfocus.application.automation.tools.octane.configuration.ConfigurationService;
import com.microfocus.application.automation.tools.octane.configuration.SSCServerConfigUtil;
import com.microfocus.application.automation.tools.octane.tests.build.BuildHandlerUtils;
import hudson.Extension;
import hudson.model.AbstractBuild;
import hudson.model.listeners.RunListener;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Jenkins events life cycle listener for processing vulnerabilities scan results on build completed
 */

@Extension
@SuppressWarnings({"squid:S2699", "squid:S3658", "squid:S2259", "squid:S1872"})
public class VulnerabilitiesListener extends RunListener<AbstractBuild> {
	private static Logger logger = LogManager.getLogger(VulnerabilitiesListener.class);

	@Override
	public void onFinalized(AbstractBuild build) {
		String sscServerUrl = SSCServerConfigUtil.getSSCServer();
		if (sscServerUrl == null || sscServerUrl.isEmpty()) {
			logger.debug("SSC configuration not found in the whole CI Server");
			return;
		}
		SSCServerConfigUtil.SSCProjectVersionPair projectVersionPair = SSCServerConfigUtil.getProjectConfigurationFromBuild(build);
		if (projectVersionPair == null) {
			logger.warn("SSC configuration not found in " + build);
			return;
		}

		String jobCiId = BuildHandlerUtils.getJobCiId(build);
		String buildCiId = BuildHandlerUtils.getBuildCiId(build);

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
						buildCiId,
						build.getStartTimeInMillis(),
						settings.getMaxTimeoutHours());
			}
		});
	}
}
