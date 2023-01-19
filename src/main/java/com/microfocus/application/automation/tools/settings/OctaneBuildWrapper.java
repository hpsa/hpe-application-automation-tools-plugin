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

import static com.microfocus.application.automation.tools.octane.events.OutputEnvironmentParametersHelper.SPLIT_SYMBOL;

public class OctaneBuildWrapper extends BuildWrapper implements Serializable {

	private String outputEnvironmentParameters;
	@DataBoundConstructor
	public OctaneBuildWrapper(String outputEnvironmentParameters) {
		setOutputEnvironmentParameters(outputEnvironmentParameters);
	}

	public String getOutputEnvironmentParameters() {
		return outputEnvironmentParameters;
	}
	@DataBoundSetter
	public void setOutputEnvironmentParameters(String outputEnvironmentParameters) {
		this.outputEnvironmentParameters = getValidatedOutputEnvironmentParameters(outputEnvironmentParameters);
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

	private String getValidatedOutputEnvironmentParameters(String envParams) {
		String[] params = envParams.split("\\s++");
		return Stream.of(params).filter(p -> !StringUtils.isNullOrEmpty(p))
				.collect(Collectors.joining(SPLIT_SYMBOL));
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
