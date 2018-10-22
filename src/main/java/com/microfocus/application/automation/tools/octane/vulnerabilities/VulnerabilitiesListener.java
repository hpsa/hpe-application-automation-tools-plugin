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

package com.microfocus.application.automation.tools.octane.vulnerabilities;

import com.hp.octane.integrations.OctaneSDK;
import com.microfocus.application.automation.tools.model.OctaneServerSettingsModel;
import com.microfocus.application.automation.tools.octane.configuration.ConfigurationService;
import com.microfocus.application.automation.tools.octane.tests.build.BuildHandlerUtils;
import hudson.Extension;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.Run;
import hudson.model.listeners.RunListener;
import hudson.tasks.Publisher;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.lang.reflect.Field;

/**
 * Jenkins events life cycle listener for processing vulnerabilities scan results on build completed
 */

@Extension
@SuppressWarnings({"squid:S2699", "squid:S3658", "squid:S2259", "squid:S1872"})
public class VulnerabilitiesListener extends RunListener<Run> {
	private static Logger logger = LogManager.getLogger(VulnerabilitiesListener.class);

	@Override
	public void onFinalized(Run run) {
		String jobCiId = BuildHandlerUtils.getJobCiId(run);
		String buildCiId = BuildHandlerUtils.getBuildCiId(run);

		//  [YG]: TODO productize the below code
//		long queueItemTimeout = 0;
//		ParametersAction parameters = run.getAction(ParametersAction.class);
//		if (parameters != null && parameters.getParameter("some-predefined-value") != null) {
//			queueItemTimeout = Long.parseLong((String) parameters.getParameter("some-predefined-value").getValue());
//		}


		OctaneSDK.getClients().forEach(octaneClient -> {
			String instanceId = octaneClient.getInstanceId();
			OctaneServerSettingsModel settings = ConfigurationService.getSettings(instanceId);
			if (settings != null && !settings.isSuspend()) {
				ProjectAndVersionJobConfig projectAndVersionJobConfig = getProjectVersionInJobConfig(run);
				if (projectAndVersionJobConfig == null) {
					logger.warn("Octane configuration is not valid");
					return;
				}

				octaneClient.getVulnerabilitiesService().enqueueRetrieveAndPushVulnerabilities(
						jobCiId,
						buildCiId,
						projectAndVersionJobConfig.project,
						projectAndVersionJobConfig.version,
						run.getStartTimeInMillis(),
						settings.getMaxTimeoutHours());
			}
		});
	}

	static class ProjectAndVersionJobConfig {
		public String project;
		public String version;

		public ProjectAndVersionJobConfig(String projectName, String projectVersion) {
			this.project = projectName;
			this.version = projectVersion;
		}
	}

	private ProjectAndVersionJobConfig getProjectVersionInJobConfig(Run run) {
		AbstractProject project = ((AbstractBuild) run).getProject();
		for (Object publisherO : project.getPublishersList()) {
			if (publisherO instanceof Publisher) {
				Publisher publisher = (Publisher) publisherO;
				publisher.getClass().getName().equals(
						"com.fortify.plugin.jenkins.FPRPublisher");
				return getProjectNameByReflection(publisherO);
			}
		}
		logger.warn("Unable to find SSC config in project configuration.");
		return null;
	}

	private ProjectAndVersionJobConfig getProjectNameByReflection(Object someObject) {
		String projectName = getFieldValue(someObject, "projectName");
		String projectVersion = getFieldValue(someObject, "projectVersion");
		if (projectName != null && projectVersion != null) {
			return new ProjectAndVersionJobConfig(projectName, projectVersion);
		}
		return null;
	}

	private String getFieldValue(Object someObject, String fieldName) {
		for (Field field : someObject.getClass().getDeclaredFields()) {
			field.setAccessible(true);
			if (field.getName().equals(fieldName)) {
				Object value = null;
				try {
					value = field.get(someObject);
				} catch (IllegalAccessException e) {
					e.printStackTrace();
				}
				if (value != null) {
					return value.toString();
				}
			}
		}
		return null;
	}
}
