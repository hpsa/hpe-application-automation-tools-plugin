package com.microfocus.application.automation.tools.settings;

import com.microfocus.application.automation.tools.octane.events.OutputEnvironmentParametersHelper;
import hudson.EnvVars;
import hudson.Extension;
import hudson.Launcher;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.BuildListener;
import hudson.tasks.BuildWrapper;
import hudson.tasks.BuildWrapperDescriptor;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;

import java.io.IOException;
import java.io.Serializable;
import java.util.Map;

public class OutputEnvironmentVariablesBuildWrapper extends BuildWrapper implements Serializable {

	private String outputEnvironmentParameters;
	@DataBoundConstructor
	public OutputEnvironmentVariablesBuildWrapper(String outputEnvironmentParameters) {
		setOutputEnvironmentParameters(outputEnvironmentParameters);
	}

	public String getOutputEnvironmentParameters() {
		return outputEnvironmentParameters;
	}
	@DataBoundSetter
	public void setOutputEnvironmentParameters(String outputEnvironmentParameters) {
		this.outputEnvironmentParameters =
				OutputEnvironmentParametersHelper.validateOutputEnvironmentParamsString(outputEnvironmentParameters);
	}

	@Override
	public Environment setUp(AbstractBuild build, Launcher launcher, BuildListener listener) throws IOException, InterruptedException {
		EnvVars envVars = build.getEnvironment(listener);
		return new Environment() {
			@Override
			public void buildEnvVars(Map<String, String> env) {
				env.putAll(envVars);
			}
		};
	}

	@Extension
	public static final class DescriptorImpl extends BuildWrapperDescriptor {

		@Override
		public boolean isApplicable(AbstractProject<?, ?> item) {
			return true;
		}

		@Override
		public String getDisplayName() {
			return "Define list of Environment Variables to be sent to ALM Octane";
		}
	}

}
