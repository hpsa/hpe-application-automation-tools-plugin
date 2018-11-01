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

package com.microfocus.application.automation.tools.octane.configuration;

import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.Descriptor;
import hudson.tasks.Publisher;
import jenkins.model.Jenkins;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.lang.reflect.Field;

/*
    Utility to help retrieving the configuration of the SSC Server URL and SSC project/version pair
 */

public class SSCServerConfigUtil {

	private static final Logger logger = LogManager.getLogger(SSCServerConfigUtil.class);

	public static String getSSCServer() {
		Descriptor sscDescriptor = getSSCDescriptor();
		return sscDescriptor != null ? getFieldValue(sscDescriptor, "url") : null;
	}

	/**
	 * extracts and returns SSC project name and version if found on the AbstractProject configuration of the specified build
	 *
	 * @param build AbstractBuild
	 * @return valid SSC project name and version pair; otherwise NULL
	 */
	public static SSCProjectVersionPair getProjectConfigurationFromBuild(AbstractBuild build) {
		return build != null ? getProjectVersion(build.getProject()) : null;
	}

	private static SSCProjectVersionPair getProjectVersion(AbstractProject project) {
		for (Object publisher : project.getPublishersList()) {
			if (publisher instanceof Publisher && "com.fortify.plugin.jenkins.FPRPublisher".equals(publisher.getClass().getName())) {
				return getProjectNameByReflection(publisher);
			}
		}
		return null;
	}

	private static SSCProjectVersionPair getProjectNameByReflection(Object fprPublisher) {
		String projectName = getFieldValue(fprPublisher, "projectName");
		String projectVersion = getFieldValue(fprPublisher, "projectVersion");
		if (projectName != null && !projectName.isEmpty() && projectVersion != null && !projectVersion.isEmpty()) {
			return new SSCProjectVersionPair(projectName, projectVersion);
		}
		return null;
	}

	private static String getFieldValue(Object someObject, String fieldName) {
		for (Field field : someObject.getClass().getDeclaredFields()) {
			field.setAccessible(true);
			if (field.getName().equals(fieldName)) {
				Object value = null;
				try {
					value = field.get(someObject);
				} catch (IllegalAccessException e) {
					logger.error("Failed to getFieldValue", e);
				}
				if (value != null) {
					return value.toString();
				}
			}
		}
		return null;
	}

	private static Descriptor getSSCDescriptor() {
		return Jenkins.getInstance().getDescriptorByName("com.fortify.plugin.jenkins.FPRPublisher");
	}

	public static final class SSCProjectVersionPair {
		public final String project;
		public final String version;

		private SSCProjectVersionPair(String project, String version) {
			this.project = project;
			this.version = version;
		}
	}
}
