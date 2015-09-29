/**
 © Copyright 2015 Hewlett Packard Enterprise Development LP

 Permission is hereby granted, free of charge, to any person obtaining a copy
 of this software and associated documentation files (the "Software"), to deal
 in the Software without restriction, including without limitation the rights
 to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 copies of the Software, and to permit persons to whom the Software is
 furnished to do so, subject to the following conditions:

 The above copyright notice and this permission notice shall be included in
 all copies or substantial portions of the Software.

 THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 FITNESS FOR A PARTICULAR PURPOSE AND NON INFRINGEMENT. IN NO EVENT SHALL THE
 AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 THE SOFTWARE.
 */
package com.hpe.application.automation.bamboo.tasks;

import com.atlassian.bamboo.build.Job;
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

		(new HpTasksArtifactRegistrator()).registerCommonArtifact((Job)context.get("plan"), getI18nBean(), this.artifactDefinitionManager);
	}
}
