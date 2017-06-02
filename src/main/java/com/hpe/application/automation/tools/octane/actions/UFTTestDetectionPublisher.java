/*
 *     Copyright 2017 Hewlett-Packard Development Company, L.P.
 *     Licensed under the Apache License, Version 2.0 (the "License");
 *     you may not use this file except in compliance with the License.
 *     You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS,
 *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *     See the License for the specific language governing permissions and
 *     limitations under the License.
 *
 */

package com.hpe.application.automation.tools.octane.actions;

import com.hpe.application.automation.tools.octane.executor.UFTTestDetectionResult;
import com.hpe.application.automation.tools.octane.executor.UFTTestDetectionService;
import com.hp.mqm.client.MqmRestClient;
import com.hp.mqm.client.model.PagedList;
import com.hp.mqm.client.model.Workspace;
import com.hpe.application.automation.tools.octane.client.JenkinsMqmRestClientFactory;
import com.hpe.application.automation.tools.octane.configuration.ConfigurationService;
import com.hpe.application.automation.tools.octane.configuration.ServerConfiguration;
import hudson.Extension;
import hudson.ExtensionList;
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
import jenkins.model.Jenkins;
import net.sf.json.JSONObject;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.StaplerRequest;

import javax.servlet.ServletException;
import java.io.IOException;
import java.util.List;

/**
 * Post-build action of Uft test detection
 */
public class UFTTestDetectionPublisher extends Recorder {

	private final String workspaceName;
	private final String scmRepositoryId;

	public String getWorkspaceName() {
		return workspaceName;
	}

	public String getScmRepositoryId() {
		return scmRepositoryId;
	}

	// Fields in config.jelly must match the parameter names in the "DataBoundConstructor"
	@DataBoundConstructor
	public UFTTestDetectionPublisher(String workspaceName, String scmRepositoryId) {

		this.workspaceName = workspaceName;
		this.scmRepositoryId = scmRepositoryId;
	}

	@Override
	public boolean perform(AbstractBuild build, Launcher launcher, BuildListener listener) {
		UFTTestDetectionResult results = UFTTestDetectionService.startScanning(build, getWorkspaceName(), getScmRepositoryId(), listener);
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

	private static <T> T getExtension(Class<T> clazz) {
		ExtensionList<T> items = Jenkins.getInstance().getExtensionList(clazz);
		return items.get(0);
	}

	@Extension // This indicates to Jenkins that this is an implementation of an extension point.
	public static final class DescriptorImpl extends BuildStepDescriptor<Publisher> {
		private MqmRestClient createClient() {
			ServerConfiguration configuration = ConfigurationService.getServerConfiguration();
			JenkinsMqmRestClientFactory clientFactory = getExtension(JenkinsMqmRestClientFactory.class);
			return clientFactory.obtain(
					configuration.location,
					configuration.sharedSpace,
					configuration.username,
					configuration.password);
		}

		private String workspace;

		public DescriptorImpl() {
			load();
		}

		public ListBoxModel doFillWorkspaceNameItems() {
			ListBoxModel m = new ListBoxModel();
			PagedList<Workspace> workspacePagedList = createClient().queryWorkspaces("", 0, 200);
			List<Workspace> items = workspacePagedList.getItems();
			for (Workspace workspace : items) {
				m.add(workspace.getName(), String.valueOf(workspace.getId()));
			}
			return m;
		}

		public FormValidation doCheckWorkspaceName(@QueryParameter String value) throws IOException, ServletException {
			if (value == null || value.length() == 0) {
				return FormValidation.error("Please select workspace");
			} else {
				return FormValidation.ok();
			}
		}

		public boolean isApplicable(Class<? extends AbstractProject> aClass) {
			// Indicates that this builder can be used with all kinds of project types

			return aClass.equals(FreeStyleProject.class);
		}

		public String getDisplayName() {
			return "HPE Octane UFT Tests Scanner";
		}

		@Override
		public boolean configure(StaplerRequest req, JSONObject formData) throws FormException {
			// To persist global configuration information,
			// set that to properties and call save().
			workspace = formData.getString("useFrench");
			// ^Can also use req.bindJSON(this, formData);
			//  (easier when there are many fields; need set* methods for this, like setUseFrench)
			save();
			return super.configure(req, formData);
		}

		public String getWorkspace() {
			return workspace;
		}

	}
}