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
import com.hpe.application.automation.tools.octane.actions.coverage.CoveragePublisherAction;
import com.hpe.application.automation.tools.octane.actions.coverage.CoverageService;
import com.hpe.application.automation.tools.octane.tests.CoverageReportsDispatcher;
import hudson.Extension;
import hudson.ExtensionList;
import hudson.Launcher;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.BuildListener;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.BuildStepMonitor;
import hudson.tasks.Publisher;
import hudson.tasks.Recorder;
import jenkins.model.Jenkins;
import org.kohsuke.stapler.DataBoundConstructor;

/**
 * Post-build action that collects the coverage reports from workspace
 * the reports that matches a specified regular expression path, are copied to
 * the build folder for future upload.
 */
public class CoveragePublisher extends Recorder {
	private final String coveragePathPattern;

	/**
	 * this ctor is being called from configuration page.
	 * the coveragePathPattern is being injected from the web page text box
	 * @param coveragePathPattern regular expression path for coverage reports
	 */
	@DataBoundConstructor
	public CoveragePublisher(String coveragePathPattern) {
		// Fields in config.jelly must match the parameter names in the "DataBoundConstructor"
		this.coveragePathPattern = coveragePathPattern;
	}

	/**
	 * this is where we build the project. this method is being called when we run the build
	 * @param build instance
	 * @param launcher instance
	 * @param listener for action attachment
	 * @return status
	 */
	@Override
	public boolean perform(AbstractBuild build, Launcher launcher, BuildListener listener) {
		ExtensionList<CoverageReportsDispatcher> extensionList = Jenkins.getInstance().getExtensionList(CoverageReportsDispatcher.class);
		if (extensionList == null || extensionList.size() == 0) {
			return false;
		}
		// copy coverage reports
		CoveragePublisherAction action = new CoveragePublisherAction(build, coveragePathPattern, listener);
		build.addAction(action);
		if (!action.copyCoverageReportsToBuildFolder()) {
			return false;
		}
		// add upload task to queue
		extensionList.get(0).enqueueTask(build.getProject().getFullName(), build.getNumber());
		return true;
	}

	/**
	 * bound between descriptor to publisher
	 * @return descriptor
	 */
	@Override
	public DescriptorImpl getDescriptor() {
		return (DescriptorImpl) super.getDescriptor();
	}

	/**
	 * Returns BuildStepMonitor.NONE by default, as Builders normally don't depend on its previous result
	 * @return monitor
	 */
	@Override
	public BuildStepMonitor getRequiredMonitorService() {
		return BuildStepMonitor.NONE;
	}

	/**
	 * The Publisher object or Recorder is the base.
	 * It needs a BuildStepDescriptor to provide certain information to Jenkins
	 */
	@Extension
	public static final class DescriptorImpl extends BuildStepDescriptor<Publisher> {

		public DescriptorImpl() {
			load();
		}

		/**
		 * Indicates that this builder can be used with all kinds of project types
		 * @param aClass
		 * @return
		 */
		public boolean isApplicable(Class<? extends AbstractProject> aClass) {
			return true; // so that it will also be available for maven & other projects
			//return aClass.equals(FreeStyleProject.class);
		}

		public String getDisplayName() {
			return "HPE Octane coverage publisher";
		}
	}
}