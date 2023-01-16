package com.microfocus.application.automation.tools.settings;

import com.microfocus.application.automation.tools.sse.common.StringUtils;
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
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class OctaneBuildWrapper extends BuildWrapper implements Serializable {

	private String outputExecutionParameters;
	@DataBoundConstructor
	public OctaneBuildWrapper(String outputExecutionParameters) {
		this.outputExecutionParameters = outputExecutionParameters;
	}

	public String getOutputExecutionParameters() {
		return outputExecutionParameters;
	}
	@DataBoundSetter
	public void setOutputExecutionParameters(String outputExecutionParameters) {
		this.outputExecutionParameters = getValidatedOutputExecutionParameters(outputExecutionParameters);
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

	private String getValidatedOutputExecutionParameters(String executionParams) {
		String[] params = executionParams.split(" ");
		return Stream.of(params).filter(p -> !StringUtils.isNullOrEmpty(p))
				.collect(Collectors.joining(" "));
	}


	@Extension
	public static final class DescriptorImpl extends BuildWrapperDescriptor {

		@Override
		public boolean isApplicable(AbstractProject<?, ?> item) {
			return true;
		}

		@Override
		public String getDisplayName() {
			return "Octane Build Environment";
		}

		@Override
		public String getHelpFile() {
			return "/plugin/hp-application-automation-tools-plugin/help/help-octaneBuildEnvConfig.html";
		}
	}

}
