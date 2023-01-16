package com.microfocus.application.automation.tools.octane.events;

import com.microfocus.application.automation.tools.settings.OctaneBuildWrapper;
import com.microfocus.application.automation.tools.settings.RunnerMiscSettingsGlobalConfiguration;
import com.microfocus.application.automation.tools.sse.common.StringUtils;
import hudson.EnvVars;
import hudson.model.AbstractBuild;
import hudson.model.BuildableItemWithBuildWrappers;
import hudson.model.Job;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class OutputExecutionParametersHelper {

	private static final String SPLIT_SYMBOL = " ";

	public static Map<String, String> getOutputExecutionParams(AbstractBuild build) {
		EnvVars environment = getEnvironment(build);
		if (environment == null) {
			return Collections.emptyMap();
		} else {
			List<String> paramKeysList = getGlobalParamsList();
			paramKeysList.addAll(getJobParamsList(build));

			if (paramKeysList.isEmpty()) return Collections.emptyMap();

			Map<String, String> outputExecutionParams = new HashMap<>();
			Set<String> sensitiveBuildVariables = build.getSensitiveBuildVariables();

			String value;
			for (String key : paramKeysList) {
				if (sensitiveBuildVariables.contains(key)) continue;
				value = environment.get(key);
				if (value != null) {
					outputExecutionParams.put(key, value);
				}
			}
			return outputExecutionParams;
		}
	}

	private static EnvVars getEnvironment(AbstractBuild build) {
		EnvVars environment = null;
		try {
			environment = build.getEnvironment(null);
		} catch (IOException | InterruptedException ignored) {
		}
		return environment;
	}

	private static List<String> getGlobalParamsList() {
		return Stream.of(RunnerMiscSettingsGlobalConfiguration.getInstance().getOutputExecutionParameters().split(SPLIT_SYMBOL))
				.filter(p -> !StringUtils.isNullOrEmpty(p)).collect(Collectors.toList());
	}

	private static List<String> getJobParamsList(AbstractBuild build) {
		Job<?, ?> job = build.getParent();
		if (job instanceof BuildableItemWithBuildWrappers) {
			String paramsStr = ((BuildableItemWithBuildWrappers) job).getBuildWrappersList().
					get(OctaneBuildWrapper.class).getOutputExecutionParameters();
			if (paramsStr != null) {
				String[] params = paramsStr.split(SPLIT_SYMBOL);
				return Stream.of(params).filter(p -> !StringUtils.isNullOrEmpty(p)).collect(Collectors.toList());
			}
		}
		return Collections.emptyList();
	}
}
