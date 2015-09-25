package com.hpe.application.automation.bamboo.tasks;

import com.atlassian.bamboo.collections.ActionParametersMap;
import com.atlassian.bamboo.plan.artifact.ArtifactDefinitionManager;
import com.atlassian.bamboo.task.TaskDefinition;
import com.atlassian.util.concurrent.NotNull;
import com.atlassian.util.concurrent.Nullable;

import java.util.Map;

public class AbstractLauncherTaskConfigurator extends AbstractUftTaskConfigurator {
	private static final String BUILD_WORKING_DIR = "bamboo.agentId";

	private ArtifactDefinitionManager artifactDefinitionManager;

	public void setArtifactDefinitionManager(ArtifactDefinitionManager artifactDefinitionManager){
		this.artifactDefinitionManager = artifactDefinitionManager;
	}

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

	@Override
	public void populateContextForCreate(@NotNull final Map<String, Object> context)
	{
		super.populateContextForCreate(context);

		(new HpTasksArtifactRegistrator()).registerCommonArtifact(context.get("plan"), getI18nBean(), this.artifactDefinitionManager);
	}
}
