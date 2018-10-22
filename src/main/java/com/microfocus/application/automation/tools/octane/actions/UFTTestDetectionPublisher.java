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

import com.hp.octane.integrations.OctaneSDK;
import com.hp.octane.integrations.dto.entities.Entity;
import com.hp.octane.integrations.services.entities.EntitiesService;
import com.hp.octane.integrations.uft.items.UftTestDiscoveryResult;
import com.microfocus.application.automation.tools.octane.configuration.ConfigurationService;
import com.microfocus.application.automation.tools.octane.executor.UFTTestDetectionService;
import hudson.Extension;
import hudson.Launcher;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.BuildListener;
import hudson.model.FreeStyleProject;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.BuildStepMonitor;
import hudson.tasks.Publisher;
import hudson.tasks.Recorder;
import hudson.util.FormValidation;
import hudson.util.ListBoxModel;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.QueryParameter;

import java.util.List;

/**
 * Post-build action of Uft test detection
 */

public class UFTTestDetectionPublisher extends Recorder {
	private final String sharedSpaceId;
	private final String workspaceName;
	private final String scmRepositoryId;

	public String getSharedSpaceId() {
		return sharedSpaceId;
	}

	public String getWorkspaceName() {
		return workspaceName;
	}

	public String getScmRepositoryId() {
		return scmRepositoryId;
	}

	// Fields in config.jelly must match the parameter names in the "DataBoundConstructor"
	@DataBoundConstructor
	public UFTTestDetectionPublisher(String sharedSpaceId, String workspaceName, String scmRepositoryId) {
		this.sharedSpaceId = sharedSpaceId;
		this.workspaceName = workspaceName;
		this.scmRepositoryId = scmRepositoryId;
	}

	@Override
	public boolean perform(AbstractBuild build, Launcher launcher, BuildListener listener) {
		UftTestDiscoveryResult results = UFTTestDetectionService.startScanning(build, getWorkspaceName(), getScmRepositoryId(), listener);
		UFTTestDetectionBuildAction buildAction = new UFTTestDetectionBuildAction(build, results);
		build.addAction(buildAction);

		return true;
	}

	@Override
	public DescriptorImpl getDescriptor() {
		return (DescriptorImpl) super.getDescriptor();
	}

	@Override
	public BuildStepMonitor getRequiredMonitorService() {
		return BuildStepMonitor.NONE;
	}

	@Extension // This indicates to Jenkins that this is an implementation of an extension point.
	public static final class DescriptorImpl extends BuildStepDescriptor<Publisher> {

		public ListBoxModel doFillSharedSpaceIdItems() {
			ListBoxModel m = new ListBoxModel();
			if (!ConfigurationService.getAllSettings().isEmpty()) {
				ConfigurationService.getAllSettings().forEach(settings -> m.add(settings.getSharedSpace(), settings.getSharedSpace()));
			}
			return m;
		}

		public FormValidation doCheckSharedSpaceId(@QueryParameter String value) {
			if (value == null || value.isEmpty()) {
				return FormValidation.error("Please select shared space");
			} else {
				return FormValidation.ok();
			}
		}

		public ListBoxModel doFillWorkspaceNameItems(@QueryParameter String sharedSpaceId) {
			ListBoxModel m = new ListBoxModel();
			if (sharedSpaceId != null && !sharedSpaceId.isEmpty()) {
				EntitiesService entitiesService = OctaneSDK.getClientBySharedSpaceId(sharedSpaceId).getEntitiesService();
				List<Entity> workspaces = entitiesService.getEntities(null, "workspaces", null, null);
				for (Entity workspace : workspaces) {
					m.add(workspace.getName(), String.valueOf(workspace.getId()));
				}
			}
			return m;
		}

		public FormValidation doCheckWorkspaceName(
				@QueryParameter(value = "workspaceName") String value,
				@QueryParameter(value = "sharedSpaceId") String sharedSpaceId) {
			if (sharedSpaceId == null || sharedSpaceId.isEmpty()) {
				return FormValidation.error("Please select shared space");
			}
			if (value == null || value.isEmpty()) {
				return FormValidation.error("Please select workspace");
			}
			return FormValidation.ok();
		}

		public boolean isApplicable(Class<? extends AbstractProject> aClass) {
			return aClass.equals(FreeStyleProject.class);
		}

		public String getDisplayName() {
			return "ALM Octane UFT Tests Scanner";
		}
	}
}
