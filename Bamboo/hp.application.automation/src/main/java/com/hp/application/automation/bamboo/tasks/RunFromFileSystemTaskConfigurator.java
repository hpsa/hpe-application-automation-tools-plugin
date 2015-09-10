package com.hp.application.automation.bamboo.tasks;

import java.util.HashMap;
import java.util.Map;
import com.atlassian.bamboo.collections.ActionParametersMap;
import com.atlassian.bamboo.task.TaskDefinition;
import com.atlassian.bamboo.utils.error.ErrorCollection;
import com.atlassian.bamboo.utils.i18n.I18nBean;
import com.atlassian.util.concurrent.NotNull;
import com.atlassian.util.concurrent.Nullable;
import org.apache.commons.lang.StringUtils;

public class RunFromFileSystemTaskConfigurator extends AbstractLauncherTaskConfigurator {

	public static final String TESTS_PATH = "testPathInput";
	public static final String TIMEOUT = "timeoutInput";

	public static final String PUBLISH_MODE_ALWAYS_STRING = "RunFromFileSystemTask.publishMode.always";
	public static final String PUBLISH_MODE_FAILED_STRING = "RunFromFileSystemTask.publishMode.failed";
	public static final String PUBLISH_MODE_NEVER_STRING = "RunFromFileSystemTask.publishMode.never";

	public static final String ARTIFACT_NAME_FORMAT_STRING = "RunFromFileSystemTask.artifactNameFormat";

	public static final String PUBLISH_MODE_PARAM = "publishMode";
	public static final String PUBLISH_MODE_ITEMS_PARAM = "publishModeItems";

	public static final String PUBLISH_MODE_ALWAYS_VALUE = "always";
	public static final String PUBLISH_MODE_FAILED_VALUE = "failed";
	public static final String PUBLISH_MODE_NEVER_VALUE = "never";
	public static final String TASK_NAME_VALUE = "RunFromFileSystemTaskConfigurator.taskName";
	private static final String TASK_NAME = "taskName";
	private static final String TASK_ID_CONTROL = "RunFromFileSystemTaskConfigurator.taskId";
	private static final String TASK_ID_LBL = "RunFromFileSystemTaskConfigurator.taskIdLbl";


	public Map<String, String> generateTaskConfigMap(@NotNull final ActionParametersMap params, @Nullable final TaskDefinition previousTaskDefinition)
	{
		final Map<String, String> config = super.generateTaskConfigMap(params, previousTaskDefinition);

		config.put(TESTS_PATH, params.getString(TESTS_PATH));
		config.put(TIMEOUT, params.getString(TIMEOUT));
		config.put(PUBLISH_MODE_PARAM, params.getString(PUBLISH_MODE_PARAM));
		config.put(TASK_NAME, getI18nBean().getText(TASK_NAME_VALUE));

		return config;
	}

	public void validate(@NotNull final ActionParametersMap params, @NotNull final ErrorCollection errorCollection)
	{
		super.validate(params, errorCollection);

		final String pathParameter = params.getString(TESTS_PATH);
		final String timeoutParameter = params.getString(TIMEOUT);

		I18nBean textProvider = getI18nBean();

		if (StringUtils.isEmpty(pathParameter))
		{
			errorCollection.addError(TESTS_PATH, textProvider.getText("RunFromFileSystemTaskConfigurator.error.testsPathIsEmpty"));
		}

		if(!StringUtils.isEmpty(timeoutParameter))
		{   	 
			if (!StringUtils.isNumeric(timeoutParameter) || Integer.parseInt(timeoutParameter) < 0 | Integer.parseInt(timeoutParameter) > 30)
			{
				errorCollection.addError(TIMEOUT, textProvider.getText("RunFromFileSystemTaskConfigurator.error.timeoutIsNotCorrect"));
			} 	   
		}
	}

	@Override
	public void populateContextForCreate(@NotNull final Map<String, Object> context) {
		(new HpTasksArtifactRegistrator()).registerCommonArtifact(context.get("plan"));

		super.populateContextForCreate(context);

		context.put(PUBLISH_MODE_PARAM, PUBLISH_MODE_FAILED_VALUE);

		populateContextForLists(context);
	}

	@Override
	public void populateContextForEdit(@NotNull final Map<String, Object> context, @NotNull final TaskDefinition taskDefinition)
	{
		super.populateContextForEdit(context, taskDefinition);

		context.put(TESTS_PATH, taskDefinition.getConfiguration().get(TESTS_PATH));
		context.put(TIMEOUT, taskDefinition.getConfiguration().get(TIMEOUT));
		context.put(PUBLISH_MODE_PARAM, taskDefinition.getConfiguration().get(PUBLISH_MODE_PARAM));
		context.put(TASK_ID_CONTROL, getI18nBean().getText(TASK_ID_LBL) + String.format("%03d",new Long(taskDefinition.getId())));

		populateContextForLists(context);
	}

	private void populateContextForLists(@org.jetbrains.annotations.NotNull final Map<String, Object> context)
	{
		context.put(PUBLISH_MODE_ITEMS_PARAM, getPublishModes());
	}

	private Map<String, String> getPublishModes()
	{
		Map<String, String> publishModesMap = new HashMap<String, String>();

		I18nBean textProvider = getI18nBean();

		publishModesMap.put(PUBLISH_MODE_FAILED_VALUE, textProvider.getText(PUBLISH_MODE_FAILED_STRING));
		publishModesMap.put(PUBLISH_MODE_ALWAYS_VALUE, textProvider.getText(PUBLISH_MODE_ALWAYS_STRING));
		publishModesMap.put(PUBLISH_MODE_NEVER_VALUE, textProvider.getText(PUBLISH_MODE_NEVER_STRING));

		return publishModesMap;
	}
}
