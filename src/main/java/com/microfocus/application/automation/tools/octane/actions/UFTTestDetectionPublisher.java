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

package com.microfocus.application.automation.tools.octane.actions;

import com.hp.octane.integrations.OctaneClient;
import com.hp.octane.integrations.OctaneSDK;
import com.hp.octane.integrations.dto.entities.Entity;
import com.hp.octane.integrations.dto.entities.EntityConstants;
import com.hp.octane.integrations.services.entities.EntitiesService;
import com.hp.octane.integrations.services.entities.QueryHelper;
import com.hp.octane.integrations.uft.items.UftTestDiscoveryResult;
import com.microfocus.application.automation.tools.model.OctaneServerSettingsModel;
import com.microfocus.application.automation.tools.octane.Messages;
import com.microfocus.application.automation.tools.octane.configuration.ConfigurationService;
import com.microfocus.application.automation.tools.octane.executor.UFTTestDetectionService;
import com.microfocus.application.automation.tools.octane.executor.UftJobRecognizer;
import hudson.Extension;
import hudson.Launcher;
import hudson.model.*;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.BuildStepMonitor;
import hudson.tasks.Publisher;
import hudson.tasks.Recorder;
import hudson.util.FormValidation;
import hudson.util.ListBoxModel;
import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.QueryParameter;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

/**
 * Post-build action of Uft test detection
 */

public class UFTTestDetectionPublisher extends Recorder {
	private String configurationId;
	private String workspaceName;
	private String scmRepositoryId;

	private static final Logger logger = LogManager.getLogger(UFTTestDetectionPublisher.class);

	public String getWorkspaceName() {
		return workspaceName;
	}

	public String getScmRepositoryId() {
		return scmRepositoryId;
	}

	// Fields in config.jelly must match the parameter names in the "DataBoundConstructor"
	@DataBoundConstructor
	public UFTTestDetectionPublisher(String configurationId, String workspaceName, String scmRepositoryId) {
		this.configurationId = configurationId;
		this.workspaceName = workspaceName;
		this.scmRepositoryId = scmRepositoryId;
	}

	@Override
	public boolean perform(AbstractBuild build, Launcher launcher, BuildListener listener) {
		if (configurationId == null) {
			logger.error("discovery configurationId is null.");
			configurationId = tryFindConfigurationId(build, Long.parseLong(workspaceName));
			if (configurationId != null) {
				try {
					build.getParent().save();
				} catch (IOException e) {
					logger.error("Failed to save UFTTestDetectionPublisher : " + e.getMessage());
				}
			} else {
				logger.error("No relevant ALM Octane configuration is found.");
				build.setResult(Result.FAILURE);
				throw new IllegalArgumentException("No relevant ALM Octane configuration is found.");
			}
		}

		UftTestDiscoveryResult results = UFTTestDetectionService.startScanning(build, configurationId, getWorkspaceName(), getScmRepositoryId(), listener);
		UFTTestDetectionBuildAction buildAction = new UFTTestDetectionBuildAction(build, results);
		build.addAction(buildAction);

		return true;
	}

	private static String tryFindConfigurationId(AbstractBuild build, long workspaceId) {
		String result = null;
		List<OctaneClient> clients = OctaneSDK.getClients();
		logger.warn("number of configurations is " + clients.size());
		if (clients.size() == 1) {
			result = OctaneSDK.getClients().get(0).getInstanceId();
			logger.warn("selecting the only configurationId - " + result);
		} else if (clients.size() > 0) {
			String executorLogicalName = UftJobRecognizer.getExecutorLogicalName((FreeStyleProject) build.getParent());
			Collection<String> conditions = Arrays.asList(QueryHelper.condition(EntityConstants.Base.LOGICAL_NAME_FIELD, executorLogicalName));
			for (OctaneClient client : clients) {
				try {
					List<Entity> entities = client.getEntitiesService().getEntities(workspaceId, EntityConstants.Executors.COLLECTION_NAME, conditions, null);
					if (!entities.isEmpty()) {
						result = client.getInstanceId();
						logger.warn("executor logical name found in configurationId - " + result);
						break;
					}
				} catch (Exception e) {
					logger.warn("Failed to check executor logical name in configuration " + client.getInstanceId() + " : " + e.getMessage());
				}
			}
		}

		return result;
	}


	@Override
	public DescriptorImpl getDescriptor() {
		return (DescriptorImpl) super.getDescriptor();
	}

	@Override
	public BuildStepMonitor getRequiredMonitorService() {
		return BuildStepMonitor.NONE;
	}

	public String getConfigurationId() {
		return configurationId;
	}

	@Extension // This indicates to Jenkins that this is an implementation of an extension point.
	public static final class DescriptorImpl extends BuildStepDescriptor<Publisher> {

		public ListBoxModel doFillConfigurationIdItems() {
			ListBoxModel m = new ListBoxModel();

			for (OctaneClient octaneClient : OctaneSDK.getClients()) {
				OctaneServerSettingsModel model = ConfigurationService.getSettings(octaneClient.getInstanceId());
				m.add(model.getCaption(), model.getIdentity());
			}
			return m;
		}

		public FormValidation doCheckConfigurationId(@QueryParameter String value) {
			if (StringUtils.isEmpty(value)) {
				return FormValidation.error("Please select configuration");
			} else {
				return FormValidation.ok();
			}
		}

		public ListBoxModel doFillWorkspaceNameItems(@QueryParameter String configurationId) {
			ListBoxModel m = new ListBoxModel();
			if (StringUtils.isNotEmpty(configurationId)) {
				try {
					EntitiesService entitiesService = OctaneSDK.getClientByInstanceId(configurationId).getEntitiesService();
					List<Entity> workspaces = entitiesService.getEntities(null, "workspaces", null, null);
					for (Entity workspace : workspaces) {
						m.add(workspace.getName(), String.valueOf(workspace.getId()));
					}
				} catch (Exception e) {
					//octane configuration not found
					return m;
				}
			}
			return m;
		}

		public FormValidation doCheckWorkspaceName(@QueryParameter(value = "workspaceName") String value) {
			if (StringUtils.isEmpty(value)) {
				return FormValidation.error("Please select workspace");
			}
			return FormValidation.ok();
		}

		public boolean isApplicable(Class<? extends AbstractProject> aClass) {
			return aClass.equals(FreeStyleProject.class);
		}

		public String getDisplayName() {
			return Messages.UFTTestDetectionPublisherConfigurationLabel();
		}
	}
}
