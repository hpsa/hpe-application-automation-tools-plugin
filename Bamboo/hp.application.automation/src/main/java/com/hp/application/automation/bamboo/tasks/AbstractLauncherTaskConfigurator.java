package com.hp.application.automation.bamboo.tasks;

import com.atlassian.bamboo.collections.ActionParametersMap;
import com.atlassian.bamboo.task.TaskDefinition;
import com.atlassian.util.concurrent.NotNull;
import com.atlassian.util.concurrent.Nullable;

import java.util.Map;

public class AbstractLauncherTaskConfigurator extends AbstractUftTaskConfigurator {
	public static final String BUILD_WORKING_DIR = "bamboo.agentId";

	public Map<String, String> generateTaskConfigMap(@NotNull final ActionParametersMap params, @Nullable final TaskDefinition previousTaskDefinition)
	{
		final Map<String, String> config = super.generateTaskConfigMap(params, previousTaskDefinition);

		config.put(BUILD_WORKING_DIR, "${bamboo.build.working.directory}");

		return config;
	}

	@Override
	public void populateContextForEdit(@NotNull final Map<String, Object> context, @NotNull final TaskDefinition taskDefinition)
	{
		super.populateContextForEdit(context, taskDefinition);

		context.put(BUILD_WORKING_DIR, taskDefinition.getConfiguration().get(BUILD_WORKING_DIR));
	}
}
