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
import com.atlassian.bamboo.utils.error.ErrorCollection;
import com.atlassian.bamboo.utils.i18n.I18nBean;
import com.atlassian.util.concurrent.NotNull;
import com.atlassian.util.concurrent.Nullable;
import org.apache.commons.lang.StringUtils;

import java.util.HashMap;
import java.util.Map;

public class RunFromAlmTaskConfigurator extends AbstractUftTaskConfigurator {

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
	public static final String TASK_NAME_VALUE = "Alm.taskName";

	private ArtifactDefinitionManager artifactDefinitionManager;

	public void setArtifactDefinitionManager(ArtifactDefinitionManager artifactDefinitionManager){
		this.artifactDefinitionManager = artifactDefinitionManager;
	}

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
		config.put(CommonTaskConfigurationProperties.TASK_NAME, getI18nBean().getText(TASK_NAME_VALUE));

		return config;
	}

	public void validate(@NotNull final ActionParametersMap params, @NotNull final ErrorCollection errorCollection)
	{
		super.validate(params, errorCollection);

		I18nBean textProvider = getI18nBean();

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
		(new HpTasksArtifactRegistrator()).registerCommonArtifact((Job)context.get("plan"), getI18nBean(), this.artifactDefinitionManager);
		super.populateContextForCreate(context);
		
		populateContextForLists(context);
	}

	private void populateContextForLists(@NotNull final Map<String, Object> context)
	{
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
		context.put(TESTING_TOOL_HOST, configuration.get(TESTING_TOOL_HOST));
		
		populateContextForLists(context);
	}

	private Map<String, String> getRunModes()
	{
		Map<String, String> runTypesMap = new HashMap<String, String>();

		I18nBean textProvider = getI18nBean();

		runTypesMap.put(RUN_LOCALLY_PARAMETER, textProvider.getText(RUN_LOCALLY_LBL));
		runTypesMap.put(RUN_ON_PLANNED_HOST_PARAMETER, textProvider.getText(RUN_ON_PLANNED_HOST_LBL));
		runTypesMap.put(RUN_REMOTELY_PARAMETER, textProvider.getText(RUN_REMOTELY_LBL));

		return runTypesMap;
	}
}
