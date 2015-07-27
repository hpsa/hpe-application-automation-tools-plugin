package com.hp.application.automation.bamboo.tasks;

import java.util.Map;
import com.atlassian.bamboo.collections.ActionParametersMap;
import com.atlassian.bamboo.task.AbstractTaskConfigurator;
import com.atlassian.bamboo.task.TaskDefinition;
import com.atlassian.bamboo.utils.error.ErrorCollection;
import com.atlassian.struts.TextProvider;
import com.atlassian.util.concurrent.NotNull;
import com.atlassian.util.concurrent.Nullable;
import org.apache.commons.lang.StringUtils; 

public class RunFromFileSystemTaskConfigurator extends AbstractLauncherTaskConfigurator {

	private TextProvider textProvider;

	private static final String TESTS_PATH = "testPathInput";
	private static final String TIMEOUT = "timeoutInput";

	public Map<String, String> generateTaskConfigMap(@NotNull final ActionParametersMap params, @Nullable final TaskDefinition previousTaskDefinition)
	{
		final Map<String, String> config = super.generateTaskConfigMap(params, previousTaskDefinition);

		config.put(TESTS_PATH, params.getString(TESTS_PATH));
		config.put(TIMEOUT, params.getString(TIMEOUT));

		return config;
	}

	public void validate(@NotNull final ActionParametersMap params, @NotNull final ErrorCollection errorCollection)
	{
		super.validate(params, errorCollection);

		final String pathParameter = params.getString(TESTS_PATH);
		final String timeoutParameter = params.getString(TIMEOUT);

		if (StringUtils.isEmpty(pathParameter))
		{
			errorCollection.addError(TESTS_PATH, textProvider.getText("RunFromFileSystemTaskConfigurator.error.testsPathIsEmpty"));
		}

		if(!StringUtils.isEmpty(timeoutParameter))
		{   	 
			if (!StringUtils.isNumeric(timeoutParameter) || Integer.parseInt(timeoutParameter) <0 | Integer.parseInt(timeoutParameter) > 30)
			{
				errorCollection.addError(TIMEOUT, textProvider.getText("RunFromFileSystemTaskConfigurator.error.timeoutIsNotCorrect"));
			} 	   
		} 
	}

	@Override
	public void populateContextForCreate(@NotNull final Map<String, Object> context)
	{
		super.populateContextForCreate(context);
	}

	@Override
	public void populateContextForEdit(@NotNull final Map<String, Object> context, @NotNull final TaskDefinition taskDefinition)
	{
		super.populateContextForEdit(context, taskDefinition);

		context.put(TESTS_PATH, taskDefinition.getConfiguration().get(TESTS_PATH));
		context.put(TIMEOUT, taskDefinition.getConfiguration().get(TIMEOUT));
	}

	@Override
	public void populateContextForView(@NotNull final Map<String, Object> context, @NotNull final TaskDefinition taskDefinition)
	{
		super.populateContextForView(context, taskDefinition);

		context.put(TESTS_PATH, taskDefinition.getConfiguration().get(TESTS_PATH));
		context.put(TIMEOUT, taskDefinition.getConfiguration().get(TIMEOUT));
	}

	public void setTextProvider(final TextProvider textProvider)
	{
		this.textProvider = textProvider;
	}   
}
