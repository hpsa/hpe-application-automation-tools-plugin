package com.microfocus.application.automation.tools.octane.events;

import com.microfocus.application.automation.tools.octane.configuration.SDKBasedLoggerProvider;
import com.microfocus.application.automation.tools.settings.OutputEnvironmentVariablesBuildWrapper;
import com.microfocus.application.automation.tools.settings.RunnerMiscSettingsGlobalConfiguration;
import com.microfocus.application.automation.tools.sse.common.StringUtils;
import hudson.EnvVars;
import hudson.model.AbstractBuild;
import hudson.model.BuildableItemWithBuildWrappers;
import hudson.model.Job;
import hudson.model.Run;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class OutputEnvironmentParametersHelper {

	private static final String SPLIT_SYMBOL = "\\s++";

	private OutputEnvironmentParametersHelper(){}

	private static Logger logger = SDKBasedLoggerProvider.getLogger(OutputEnvironmentParametersHelper.class);

	public static Map<String, String> getOutputEnvironmentParams(Run run) {
		EnvVars environment = getEnvironment(run);
		if (environment == null) {
			return Collections.emptyMap();
		} else {
			List<String> paramKeysList = new ArrayList<>();
			paramKeysList.addAll(getGlobalParamsList());
			paramKeysList.addAll(getJobParamsList(run));

			if (paramKeysList.isEmpty()) return Collections.emptyMap();

			Map<String, String> outputEnvParams = new HashMap<>();
			Set<String> sensitiveBuildVariables =null;
			if (run instanceof AbstractBuild) {
				sensitiveBuildVariables = ((AbstractBuild) run).getSensitiveBuildVariables();
			}

			String value;
			for (String key : paramKeysList) {
				if (sensitiveBuildVariables != null && sensitiveBuildVariables.contains(key)) continue;
				value = environment.get(key);
				if (value != null) {
					outputEnvParams.put(key, value);
				}
			}
			return outputEnvParams;
		}
	}

	private static EnvVars getEnvironment(Run run) {
		EnvVars environment = null;
		try {
			environment = run.getEnvironment(null);
		} catch (IOException | InterruptedException e) {
			logger.error("Can not get Run(id: " + run.getId() + ") Environment: " + e.getMessage());
		}
		return environment;
	}

	private static List<String> getGlobalParamsList() {
		try {
			String paramsStr = RunnerMiscSettingsGlobalConfiguration.getInstance().getOutputEnvironmentParameters();
			return createParamsListFromString(paramsStr);
		} catch (NullPointerException ignored) {
			return Collections.emptyList();
		}
	}

	private static List<String> getJobParamsList(Run run) {
		Job<?, ?> job = run.getParent();
		if (job instanceof BuildableItemWithBuildWrappers) {
			OutputEnvironmentVariablesBuildWrapper outputEnvVarsBuildWrapper = ((BuildableItemWithBuildWrappers) job)
					.getBuildWrappersList().get(OutputEnvironmentVariablesBuildWrapper.class);
			if (outputEnvVarsBuildWrapper != null) {
				String paramsStr = outputEnvVarsBuildWrapper.getOutputEnvironmentParameters();
				if (!StringUtils.isNullOrEmpty(paramsStr)) {
					return createParamsListFromString(paramsStr);
				}
			}
		}
		return Collections.emptyList();
	}

	private static List<String> createParamsListFromString(String params) {
		return Stream.of(params.split(SPLIT_SYMBOL)).filter(p -> !StringUtils.isNullOrEmpty(p)).collect(Collectors.toList());
	}
}
