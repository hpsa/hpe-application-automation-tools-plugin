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

package com.microfocus.application.automation.tools.octane.model;

import hudson.EnvVars;
import hudson.maven.MavenModuleSet;
import hudson.maven.MavenModuleSetBuild;
import hudson.model.*;
import hudson.plugins.sonar.SonarGlobalConfiguration;
import hudson.plugins.sonar.SonarInstallation;
import hudson.plugins.sonar.SonarRunnerBuilder;
import hudson.tasks.Builder;
import hudson.util.DescribableList;
import jenkins.model.GlobalConfiguration;

import java.io.IOException;
import java.util.Arrays;
import java.util.Optional;

/**
 * the adapter enables usage of sonar classes and interfaces without adding a full dependency on
 * sonar plugin.
 * this class is only dependent on sonar plugin for compile time.
 */

public class SonarHelper {
	public static final String SONAR_GLOBAL_CONFIG = "hudson.plugins.sonar.SonarGlobalConfiguration";
	private static final String SONAR_ACTION_ID = "hudson.plugins.sonar.SonarRunnerBuilder";
	private static final String SONAR_SERVER_HOST_VARIABLE = "SONAR_HOST_URL";
	private static final String SONAR_SERVER_TOKEN_VARIABLE = "SONAR_AUTH_TOKEN";

	private String serverUrl;
	private String serverToken;

	public SonarHelper(Run<?, ?> run, TaskListener listener) {
		DescribableList<Builder, Descriptor<Builder>> postbuilders = null;
		if (run instanceof AbstractBuild) {
			AbstractBuild abstractBuild = (AbstractBuild) run;
			if (abstractBuild instanceof MavenModuleSetBuild) {
				MavenModuleSet project = ((MavenModuleSetBuild) run).getProject();
				setSonarDetailsFromMavenEnvironment(abstractBuild, listener);
				postbuilders = project.getPostbuilders();
			} else {
				AbstractProject project = abstractBuild.getProject();
				if (project instanceof Project) {
					postbuilders = ((Project) project).getBuildersList();
				}
			}
			// for jobs that does not use new sonar approach for maven jobs,
			// extract data for old approach which used post build step
			if (postbuilders != null && (this.getServerUrl().isEmpty() || this.getServerToken().isEmpty())) {
				setDataFromSonarBuilder(postbuilders);
			}
		}
	}

	public String getServerUrl() {
		return serverUrl;
	}

	public String getServerToken() {
		return serverToken;
	}

	// used by the web hook
	public static String getSonarInstallationTokenByUrl(GlobalConfiguration sonarConfiguration, String sonarUrl) {
		if (sonarConfiguration instanceof SonarGlobalConfiguration) {
			SonarGlobalConfiguration sonar = (SonarGlobalConfiguration) sonarConfiguration;
			Optional<SonarInstallation> installation = Arrays.stream(sonar.getInstallations())
					.filter(sonarInstallation -> sonarInstallation.getServerUrl().equals(sonarUrl))
					.findFirst();
			if (installation.isPresent()) {
				return installation.get().getServerAuthenticationToken();
			}
		}
		return "";
	}

	private void setSonarDetailsFromMavenEnvironment(AbstractBuild build, TaskListener listener) {
		EnvVars environment;
		try {
			environment = build.getEnvironment(listener);
			if (environment != null) {
				this.serverUrl = environment.get(SONAR_SERVER_HOST_VARIABLE, "");
				this.serverToken = environment.get(SONAR_SERVER_TOKEN_VARIABLE, "");
			}
		} catch (IOException | InterruptedException e) {
			e.printStackTrace();
		}
	}

	private void setDataFromSonarBuilder(DescribableList<Builder, Descriptor<Builder>> postBuilders) {
		Builder sonarBuilder = postBuilders.getDynamic(SONAR_ACTION_ID);
		if (sonarBuilder != null) {
			SonarRunnerBuilder builder = (SonarRunnerBuilder) sonarBuilder;
			this.serverUrl = extractSonarUrl(builder);
			this.serverToken = extractSonarToken(builder);
		}
	}

	/**
	 * get sonar URL address
	 *
	 * @return Sonar's URL
	 */
	private String extractSonarUrl(SonarRunnerBuilder builder) {
		return builder != null ? builder.getSonarInstallation().getServerUrl() : "";
	}

	/**
	 * get sonar server token
	 *
	 * @return Sonar's auth token
	 */
	private String extractSonarToken(SonarRunnerBuilder builder) {
		return builder != null ? builder.getSonarInstallation().getServerAuthenticationToken() : "";
	}
}
