package com.hp.application.automation.bamboo.tasks;

import java.util.HashMap;
import java.util.Map;
import com.atlassian.bamboo.collections.ActionParametersMap;
import com.atlassian.bamboo.task.AbstractTaskConfigurator;
import com.atlassian.bamboo.task.TaskDefinition;
import com.atlassian.bamboo.utils.error.ErrorCollection;
import com.atlassian.bamboo.ww2.actions.build.admin.create.UIConfigSupport;
import com.atlassian.struts.TextProvider;
import com.atlassian.util.concurrent.NotNull;
import com.atlassian.util.concurrent.Nullable;
import org.apache.commons.lang.StringUtils;

public class RunFromAlmTaskConfigurator extends AbstractTaskConfigurator {

	private TextProvider textProvider;
	private UIConfigSupport uiConfigBean;

	private static final String UI_CONFIG_BEAN_PARAM = "uiConfigBean";
	public static final String ALM_SERVER = "almServer";
	public static final String USER_NAME = "userName";
	public static final String PASSWORD = "password";
	public static final String DOMAIN = "domain";
	public static final String PROJECT = "projectName";
	public static final String TESTS_PATH = "testPathInput";
	public static final String TIMEOUT = "timeoutInput";
	public static final String RUN_MODE = "runMode";
	public static final String RUN_MODE_PARAMETER = "runModeItems";
	public static final String TESTING_TOOL_HOST = "testingToolHost";
	public static final String DEFAULT_TIMEOUT = "-1";
	public static final String RUN_LOCALLY_LBL = "Alm.runLocallyLbl";
	public static final String RUN_ON_PLANNED_HOST_LBL = "Alm.runOnPlannedHostLbl";
	public static final String RUN_REMOTELY_LBL = "Alm.runRemotelyLbl";

	public static final String RUN_LOCALLY_PARAMETER = "1";
	public static final String RUN_ON_PLANNED_HOST_PARAMETER = "2";
	public static final String RUN_REMOTELY_PARAMETER = "3";

	public Map<String, String> generateTaskConfigMap(@NotNull final ActionParametersMap params, @Nullable final TaskDefinition previousTaskDefinition)
	{
		final Map<String, String> config = super.generateTaskConfigMap(params, previousTaskDefinition);

	    config.put(ALM_SERVER, params.getString(ALM_SERVER));
	    config.put(USER_NAME, params.getString(USER_NAME));
	    config.put(PASSWORD, params.getString(PASSWORD));
	    config.put(DOMAIN, params.getString(DOMAIN));
	    config.put(PROJECT, params.getString(PROJECT));
		config.put(TESTS_PATH, params.getString(TESTS_PATH));
		config.put(TIMEOUT, params.getString(TIMEOUT));
		config.put(RUN_MODE, params.getString(RUN_MODE));
		config.put(TESTING_TOOL_HOST, params.getString(TESTING_TOOL_HOST));

		return config;
	}

	public void validate(@NotNull final ActionParametersMap params, @NotNull final ErrorCollection errorCollection)
	{
		super.validate(params, errorCollection);

	    if (StringUtils.isEmpty(params.getString(ALM_SERVER))) {
	        errorCollection.addError(ALM_SERVER, textProvider.getText("Alm.error.ALMServerIsEmpty"));
	    }
	    if (StringUtils.isEmpty(params.getString(USER_NAME))) {
	        errorCollection.addError(USER_NAME, textProvider.getText("Alm.error.userNameIsEmpty"));
	    }
	    if (StringUtils.isEmpty(params.getString(DOMAIN))) {
	        errorCollection.addError(DOMAIN, textProvider.getText("Alm.error.domainIsEmpty"));
	    }
	    if (StringUtils.isEmpty(params.getString(PROJECT))) {
	        errorCollection.addError(PROJECT, textProvider.getText("Alm.error.projectIsEmpty"));
	    }
		
		if (StringUtils.isEmpty(params.getString(TESTS_PATH)))
		{
			errorCollection.addError(TESTS_PATH, textProvider.getText("Alm.error.testsetIsEmpty"));
		}
	    String timeoutParameter = params.getString(TIMEOUT);
		if(!StringUtils.isEmpty(timeoutParameter))
		{   	 
			if (!StringUtils.isNumeric(timeoutParameter) || Integer.parseInt(timeoutParameter) <0 | Integer.parseInt(timeoutParameter) > 30)
			{
				errorCollection.addError(TIMEOUT, textProvider.getText("Alm.error.timeoutIsNotCorrect"));
			} 	   
		} 
	}

	@Override
	public void populateContextForCreate(@NotNull final Map<String, Object> context)
	{
		super.populateContextForCreate(context);
		
		populateContextForLists(context);
	}

	private void populateContextForLists(@NotNull final Map<String, Object> context)
	{
		context.put(UI_CONFIG_BEAN_PARAM, uiConfigBean);
		context.put(RUN_MODE_PARAMETER, getRunModes());
	} 
	
	@Override
	public void populateContextForEdit(@NotNull final Map<String, Object> context, @NotNull final TaskDefinition taskDefinition)
	{
		super.populateContextForEdit(context, taskDefinition);

		Map<String, String> configuration = taskDefinition.getConfiguration();

	    context.put(ALM_SERVER, configuration.get(ALM_SERVER));
	    context.put(USER_NAME, configuration.get(USER_NAME));
	    context.put(PASSWORD, configuration.get(PASSWORD));
	    context.put(DOMAIN, configuration.get(DOMAIN));
	    context.put(PROJECT, configuration.get(PROJECT));
		context.put(TESTS_PATH, configuration.get(TESTS_PATH));
		context.put(TIMEOUT, configuration.get(TIMEOUT));
		context.put(RUN_MODE, configuration.get(RUN_MODE));
		
		populateContextForLists(context);
	}
	
	public void setTextProvider(final TextProvider textProvider)
	{
		this.textProvider = textProvider;
	}   
	
	public void setUIConfigBean(final UIConfigSupport uiConfigBean)
	{
		this.uiConfigBean = uiConfigBean;
	}

	private Map<String, String> getRunModes()
	{
		Map<String, String> runTypesMap = new HashMap<String, String>();

		runTypesMap.put(RUN_LOCALLY_PARAMETER, this.textProvider.getText(RUN_LOCALLY_LBL));
		runTypesMap.put(RUN_ON_PLANNED_HOST_PARAMETER, this.textProvider.getText(RUN_ON_PLANNED_HOST_LBL));
		runTypesMap.put(RUN_REMOTELY_PARAMETER, this.textProvider.getText(RUN_REMOTELY_LBL));

		return runTypesMap;
	}
}
