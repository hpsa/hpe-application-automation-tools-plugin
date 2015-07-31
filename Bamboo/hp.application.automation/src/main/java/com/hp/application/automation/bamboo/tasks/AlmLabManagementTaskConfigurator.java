package com.hp.application.automation.bamboo.tasks;

import com.atlassian.bamboo.collections.ActionParametersMap;
import com.atlassian.bamboo.task.AbstractTaskConfigurator;
import com.atlassian.bamboo.task.TaskDefinition;
import com.atlassian.bamboo.utils.error.ErrorCollection;
import com.atlassian.bamboo.ww2.actions.build.admin.create.UIConfigBean;
import com.atlassian.bamboo.ww2.actions.build.admin.create.UIConfigSupport;
import com.hp.application.automation.tools.common.model.CdaDetails;
import com.hp.application.automation.tools.common.model.EnumDescription;
import com.hp.application.automation.tools.common.model.SseModel;
import com.opensymphony.xwork2.*;

import org.apache.commons.lang.*;
import org.jetbrains.annotations.*;

import java.util.HashMap;
import java.util.Map;

public class AlmLabManagementTaskConfigurator extends AbstractTaskConfigurator
{
	
	public static final String ALM_SERVER_PARAM = "almServer";
	public static final String USER_NAME_PARAM = "userName";
	public static final String PASSWORD_PARAM = "password";
	public static final String DOMAIN_PARAM = "domain";
	public static final String PROJECT_NAME_PARAM = "projectName";
	public static final String RUN_TYPE_PARAM = "runType";
	public static final String TEST_ID_PARAM = "testId";
	public static final String DESCRIPTION_PARAM = "description";
	public static final String DURATION_PARAM = "duration";
	public static final String ENVIROMENT_ID_PARAM = "enviromentId";
	public static final String USE_SDA_PARAM = "useSda";
	public static final String DEPLOYMENT_ACTION_PARAM = "deploymentAction";
	public static final String DEPOYED_ENVIROMENT_NAME_PARAM = "deployedEnvironmentName";
	public static final String DEPROVISIONING_ACTION_PARAM = "deprovisioningAction";

	public static final String UI_CONFIG_BEAN_PARAM = "uiConfigBean";
	public static final String RUN_TYPE_ITEMS_PARAM = "runTypeItems";
	public static final String DEPLOYMENT_ACTION_ITEMS_PARAM = "deploymentActionItems";
	public static final String DEPROVISIONING_ACTION_ITEMS_PARAM = "deprovisioningActionItems";

	private static final String RUN_TYPE_TEST_SET_STRING = "AlmLabManagementTask.runType.item.testSet";
	private static final String RUN_TYPE_BUILD_VERIFICATION_SUITE_STRING = "AlmLabManagementTask.runType.item.buildVerificationSuite";
	
	private static final String ALM_SERVER_REQUIRED_STRING = "AlmLabManagementTask.almServer.required";
	private static final String USER_NAME_REQUIRED_STRING = "AlmLabManagementTask.userName.required";
	private static final String DOMAIN_REQUIRED_STRING = "AlmLabManagementTask.domain.required";
	private static final String PROJECT_NAME_REQIRED_STRING = "AlmLabManagementTask.projectName.required";
	private static final String TEST_ID_REQIRED_STRING = "AlmLabManagementTask.testId.required";
	private static final String DURATION_REQIRED_STRING = "AlmLabManagementTask.duration.required";
	private static final String DURATION_MINIMUM_STRING = "AlmLabManagementTask.duration.minimum";
	private static final String DURATION_INVALID_FORMAT_STRING = "AlmLabManagementTask.duration.invalidFormat";
	
	private TextProvider _textProvider;
	
	protected UIConfigSupport _uiConfigBean;
	
	public void setUiConfigBean(final UIConfigSupport uiConfigBean)
	{
		this._uiConfigBean = uiConfigBean;
	}
	
	@NotNull
    @Override
	public Map<String, String> generateTaskConfigMap(@NotNull final ActionParametersMap params, @Nullable final TaskDefinition previousTaskDefinition)
	{
	    final Map<String, String> config = super.generateTaskConfigMap(params, previousTaskDefinition);
	    
	    config.put(ALM_SERVER_PARAM, params.getString(ALM_SERVER_PARAM));
	    config.put(USER_NAME_PARAM, params.getString(USER_NAME_PARAM));
	    config.put(PASSWORD_PARAM, params.getString(PASSWORD_PARAM));
	    config.put(DOMAIN_PARAM, params.getString(DOMAIN_PARAM));
	    config.put(PROJECT_NAME_PARAM, params.getString(PROJECT_NAME_PARAM));
	    config.put(RUN_TYPE_PARAM, params.getString(RUN_TYPE_PARAM));	    
	    config.put(TEST_ID_PARAM, params.getString(TEST_ID_PARAM));
	    config.put(DESCRIPTION_PARAM, params.getString(DESCRIPTION_PARAM));
	    config.put(DURATION_PARAM, params.getString(DURATION_PARAM));
	    config.put(ENVIROMENT_ID_PARAM, params.getString(ENVIROMENT_ID_PARAM));
	    config.put(USE_SDA_PARAM, params.getString(USE_SDA_PARAM));
		config.put(DEPLOYMENT_ACTION_PARAM, params.getString((DEPLOYMENT_ACTION_PARAM)));
		config.put(DEPOYED_ENVIROMENT_NAME_PARAM, params.getString((DEPOYED_ENVIROMENT_NAME_PARAM)));
		config.put(DEPROVISIONING_ACTION_PARAM, params.getString((DEPROVISIONING_ACTION_PARAM)));
	     	    
	    return config;
	}
	
	@Override
	public void validate(@NotNull final ActionParametersMap params, @NotNull final ErrorCollection errorCollection)
	{
	    super.validate(params, errorCollection);
	    
	    String almServer = params.getString(ALM_SERVER_PARAM);
	    if(StringUtils.isEmpty(almServer)){
	    	errorCollection.addError(ALM_SERVER_PARAM, _textProvider.getText(ALM_SERVER_REQUIRED_STRING));
	    }
	 
	    String userName = params.getString(USER_NAME_PARAM);
	    if(StringUtils.isEmpty(userName)){
	    	errorCollection.addError(USER_NAME_PARAM, _textProvider.getText(USER_NAME_REQUIRED_STRING));
	    }
	    
	    String domain = params.getString(DOMAIN_PARAM);
	    if(StringUtils.isEmpty(domain)){
	    	errorCollection.addError(DOMAIN_PARAM, _textProvider.getText(DOMAIN_REQUIRED_STRING));
	    }
	    
	    String projectName = params.getString(PROJECT_NAME_PARAM);
	    if(StringUtils.isEmpty(projectName)){
	    	errorCollection.addError(PROJECT_NAME_PARAM, _textProvider.getText(PROJECT_NAME_REQIRED_STRING));
	    }
	    
	    String testId = params.getString(TEST_ID_PARAM);
	    if(StringUtils.isEmpty(testId)){
	    	errorCollection.addError(TEST_ID_PARAM, _textProvider.getText(TEST_ID_REQIRED_STRING));
	    }
	    
	    String duration = params.getString(DURATION_PARAM);
	    if(StringUtils.isEmpty(duration)){
	    	errorCollection.addError(DURATION_PARAM, _textProvider.getText(DURATION_REQIRED_STRING));
	    }
	    else
	    {
	    	try
	    	{
	    		int durationInt = Integer.parseInt(duration);
	    		if(durationInt < 30)
	    		{
	    			errorCollection.addError(DURATION_PARAM, _textProvider.getText(DURATION_MINIMUM_STRING));
	    		}
	    	}
	    	catch(NumberFormatException ex)
	    	{
	    		errorCollection.addError(DURATION_PARAM, _textProvider.getText(DURATION_INVALID_FORMAT_STRING));
	    	}
	    }	    
	}
	
	@Override
	public void populateContextForCreate(@NotNull final java.util.Map<String,Object> context)
	{
		super.populateContextForCreate(context);
		
		populateContextForLists(context);		
	};
	
	@Override
	public void populateContextForEdit(@NotNull final Map<String, Object> context, @NotNull final TaskDefinition taskDefinition)
	{
	    super.populateContextForEdit(context, taskDefinition);
	 
	    context.put(ALM_SERVER_PARAM, taskDefinition.getConfiguration().get(ALM_SERVER_PARAM));
	    context.put(USER_NAME_PARAM, taskDefinition.getConfiguration().get(USER_NAME_PARAM));
	    context.put(PASSWORD_PARAM, taskDefinition.getConfiguration().get(PASSWORD_PARAM));
	    context.put(DOMAIN_PARAM, taskDefinition.getConfiguration().get(DOMAIN_PARAM));
	    context.put(PROJECT_NAME_PARAM, taskDefinition.getConfiguration().get(PROJECT_NAME_PARAM));
	    context.put(RUN_TYPE_PARAM, taskDefinition.getConfiguration().get(RUN_TYPE_PARAM));
	    context.put(TEST_ID_PARAM, taskDefinition.getConfiguration().get(TEST_ID_PARAM));
	    context.put(DESCRIPTION_PARAM, taskDefinition.getConfiguration().get(DESCRIPTION_PARAM));
	    context.put(DURATION_PARAM, taskDefinition.getConfiguration().get(DURATION_PARAM));
	    context.put(ENVIROMENT_ID_PARAM, taskDefinition.getConfiguration().get(ENVIROMENT_ID_PARAM));
	    context.put(USE_SDA_PARAM, taskDefinition.getConfiguration().get(USE_SDA_PARAM));
		context.put(DEPLOYMENT_ACTION_PARAM, taskDefinition.getConfiguration().get((DEPLOYMENT_ACTION_PARAM)));
		context.put(DEPOYED_ENVIROMENT_NAME_PARAM, taskDefinition.getConfiguration().get((DEPOYED_ENVIROMENT_NAME_PARAM)));
		context.put(DEPROVISIONING_ACTION_PARAM, taskDefinition.getConfiguration().get((DEPROVISIONING_ACTION_PARAM)));
	    
	    populateContextForLists(context);
	}
	
	public void setTextProvider(final TextProvider textProvider)
    {
        this._textProvider = textProvider;
    }
	
	private void populateContextForLists(@NotNull final Map<String, Object> context)
	{
		context.put(UI_CONFIG_BEAN_PARAM, _uiConfigBean);	    
	    context.put(RUN_TYPE_ITEMS_PARAM, getRunTypes());
		context.put(DEPLOYMENT_ACTION_ITEMS_PARAM, getDeploymentActions());
		context.put(DEPROVISIONING_ACTION_ITEMS_PARAM, getDeprovisioningActions());
	} 
	
	private Map<String, String> getRunTypes()
	{
		Map<String, String> runTypesMap = new HashMap<String, String>();

		for (EnumDescription description : SseModel.getRunTypes())
		{
			runTypesMap.put(description.getValue(), description.getDescription());
		}
	    
	    return runTypesMap;
	}

	private Map<String, String> getDeploymentActions()
	{
		Map<String, String> deploymentActionsMap = new HashMap<String, String>();

		for (EnumDescription description : CdaDetails.getDeploymentActions())
		{
			deploymentActionsMap.put(description.getValue(), description.getDescription());
		}

		return deploymentActionsMap;
	}

	private Map<String, String> getDeprovisioningActions()
	{
		Map<String, String> deprovisioningActionMap = new HashMap<String, String>();

		for (EnumDescription description : CdaDetails.getDeprovisioningActions())
		{
			deprovisioningActionMap.put(description.getValue(), description.getDescription());
		}

		return deprovisioningActionMap;
	}
}