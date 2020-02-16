/*
 * Certain versions of software and/or documents ("Material") accessible here may contain branding from
 * Hewlett-Packard Company (now HP Inc.) and Hewlett Packard Enterprise Company.  As of September 1, 2017,
 * the Material is now offered by Micro Focus, a separately owned and operated company.  Any reference to the HP
 * and Hewlett Packard Enterprise/HPE marks is historical in nature, and the HP and Hewlett Packard Enterprise/HPE
 * marks are the property of their respective owners.
 * __________________________________________________________________
 * MIT License
 *
 * (c) Copyright 2012-2019 Micro Focus or one of its affiliates.
 *
 * The only warranties for products and services of Micro Focus and its affiliates
 * and licensors ("Micro Focus") are set forth in the express warranty statements
 * accompanying such products and services. Nothing herein should be construed as
 * constituting an additional warranty. Micro Focus shall not be liable for technical
 * or editorial errors or omissions contained herein.
 * The information contained herein is subject to change without notice.
 * ___________________________________________________________________
 */

package com.microfocus.application.automation.tools.octane.events;

import com.hp.octane.integrations.OctaneSDK;
import com.hp.octane.integrations.dto.causes.CIEventCause;
import com.hp.octane.integrations.dto.causes.CIEventCauseType;
import com.microfocus.application.automation.tools.model.OctaneServerSettingsModel;
import com.microfocus.application.automation.tools.octane.configuration.ConfigurationService;
import com.microfocus.application.automation.tools.octane.configuration.SDKBasedLoggerProvider;
import com.microfocus.application.automation.tools.octane.model.CIEventCausesFactory;
import com.microfocus.application.automation.tools.octane.tests.build.BuildHandlerUtils;
import hudson.model.Run;
import org.apache.logging.log4j.Logger;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class BuildLogHelper {
	private static Logger logger = SDKBasedLoggerProvider.getLogger(BuildLogHelper.class);

	private BuildLogHelper(){
		//for code climate
	}

	public static void enqueueBuildLog(Run run) {
		if(!OctaneSDK.hasClients()){
			return;
		}
		try {
			String jobCiId = BuildHandlerUtils.getJobCiId(run);
			String buildCiId = BuildHandlerUtils.getBuildCiId(run);
			Set<String> parents = getRootJobCiIds(run);

			logger.info("enqueued build '" + jobCiId + " #" + buildCiId + "' for log submission");
			OctaneSDK.getClients().forEach(octaneClient -> {
				String instanceId = octaneClient.getInstanceId();
				OctaneServerSettingsModel settings = ConfigurationService.getSettings(instanceId);
				if (settings != null && !settings.isSuspend()) {
					octaneClient.getLogsService().enqueuePushBuildLog(jobCiId, buildCiId, String.join(";", parents));
				}
			});
		} catch (Exception t) {
			logger.error("failed to enqueue " + run + " for logs push to Octane", t);
		}
	}

	private static Set<String> getRootJobCiIds(Run<?, ?> run) {
		Set<String> parents = new HashSet<>();
		getRootJobCiIds(CIEventCausesFactory.processCauses(run), BuildHandlerUtils.getJobCiId(run), parents);
		return parents;
	}

	private static void getRootJobCiIds(List<CIEventCause> causes, String prevUpstream, Set<String> parents) {
		if (causes != null) {
			for (CIEventCause cause : causes) {
				if (CIEventCauseType.UPSTREAM.equals(cause.getType())) {
					getRootJobCiIds(cause.getCauses(), cause.getProject(), parents);
				} else {
					if (prevUpstream != null && !prevUpstream.isEmpty()) {
						parents.add(prevUpstream);
					}
				}
			}
		}
	}
}
