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

public class AlmLabEnvPrepareTaskConfigurator extends AbstractTaskConfigurator {
	 
	 private TextProvider textProvider;
	 
	 private static final String ALM_SERVER = "almServer";
	 private static final String USER_NAME = "userName";
	 private static final String PASSWORD = "password";
	 private static final String DOMAIN = "domain";
	 private static final String PROJECT = "project";
	 private static final String AUT_ENV_ID = "AUTEnvID"; 
	 private static final String PATH_TO_JSON_FILE = "pathToJSONFile";
	 private static final String ASSIGN_ENV_CONF_ID = "assignAUTEnvConfIDto";
	 private static final String CREATE_NEW_CONF_TEXTFIELD = "createNewConf";
	 private static final String CREATE_NEW_CONF_CHECKBOX = "createNewConfChckbx";
	 private static final String USE_AN_EXISTING_CONF_TEXTFIELD = "useAnExistingConf";
	 private static final String USE_AN_EXISTING_CONF_CHECKBOX = "useAnExistingConfChckbx";
	 
	 public Map<String, String> generateTaskConfigMap(@NotNull final ActionParametersMap params, @Nullable final TaskDefinition previousTaskDefinition)
	 {
	     final Map<String, String> config = super.generateTaskConfigMap(params, previousTaskDefinition);
	  
	     config.put(ALM_SERVER, params.getString(ALM_SERVER));
	     config.put(USER_NAME, params.getString(USER_NAME));
	     config.put(PASSWORD, params.getString(PASSWORD));
	     config.put(DOMAIN, params.getString(DOMAIN));
	     config.put(PROJECT, params.getString(PROJECT));
	     config.put(CREATE_NEW_CONF_CHECKBOX, params.getString(CREATE_NEW_CONF_CHECKBOX));
	     config.put(CREATE_NEW_CONF_TEXTFIELD, params.getString(CREATE_NEW_CONF_TEXTFIELD));
	     config.put(USE_AN_EXISTING_CONF_CHECKBOX, params.getString(USE_AN_EXISTING_CONF_CHECKBOX));
	     config.put(USE_AN_EXISTING_CONF_TEXTFIELD, params.getString(USE_AN_EXISTING_CONF_TEXTFIELD));
	     config.put(PATH_TO_JSON_FILE, params.getString(PATH_TO_JSON_FILE));
	     config.put(ASSIGN_ENV_CONF_ID, params.getString(ASSIGN_ENV_CONF_ID));
	     
	     return config;
	 }
	 
	 public void validate(@NotNull final ActionParametersMap params, @NotNull final ErrorCollection errorCollection)
	 {
	     super.validate(params, errorCollection);
	     
	     if (StringUtils.isEmpty(params.getString(ALM_SERVER)))
	     {
	         errorCollection.addError(ALM_SERVER, textProvider.getText("almServerIsEmpty"));
	     }
	     if (StringUtils.isEmpty(params.getString(USER_NAME)))
	     {
	         errorCollection.addError(USER_NAME, textProvider.getText("userNameIsEmpty"));
	     }
	     if (StringUtils.isEmpty(params.getString(DOMAIN)))
	     {
	         errorCollection.addError(DOMAIN, textProvider.getText("domainIsEmpty"));
	     }
	     if (StringUtils.isEmpty(params.getString(PROJECT)))
	     {
	         errorCollection.addError(PROJECT, textProvider.getText("projectIsEmpty"));
	     }
	     if (StringUtils.isEmpty(params.getString(AUT_ENV_ID)))
	     {
	         errorCollection.addError(AUT_ENV_ID, textProvider.getText("AUTEnvIDIsEmpty"));
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
	  
	     populateContext(context, taskDefinition);
	 }
	  
	 @Override
	 public void populateContextForView(@NotNull final Map<String, Object> context, @NotNull final TaskDefinition taskDefinition)
	 {
	     super.populateContextForView(context, taskDefinition);
	     
	     populateContext(context, taskDefinition);
	 }
	 
	 private void populateContext(@NotNull final Map<String, Object> context, @NotNull final TaskDefinition taskDefinition)
	 {
	     context.put(ALM_SERVER, taskDefinition.getConfiguration().get(ALM_SERVER));
	     context.put(USER_NAME, taskDefinition.getConfiguration().get(USER_NAME));
	     context.put(PASSWORD, taskDefinition.getConfiguration().get(PASSWORD));
	     context.put(DOMAIN, taskDefinition.getConfiguration().get(DOMAIN));
	     context.put(PROJECT, taskDefinition.getConfiguration().get(PROJECT));
	     context.put(CREATE_NEW_CONF_CHECKBOX, taskDefinition.getConfiguration().get(CREATE_NEW_CONF_CHECKBOX));
	     context.put(CREATE_NEW_CONF_TEXTFIELD, taskDefinition.getConfiguration().get(CREATE_NEW_CONF_TEXTFIELD));
	     context.put(USE_AN_EXISTING_CONF_CHECKBOX, taskDefinition.getConfiguration().get(USE_AN_EXISTING_CONF_CHECKBOX));
	     context.put(USE_AN_EXISTING_CONF_TEXTFIELD, taskDefinition.getConfiguration().get(USE_AN_EXISTING_CONF_TEXTFIELD));
	     context.put(PATH_TO_JSON_FILE, taskDefinition.getConfiguration().get(PATH_TO_JSON_FILE));
	     context.put(ASSIGN_ENV_CONF_ID, taskDefinition.getConfiguration().get(ASSIGN_ENV_CONF_ID));
	 }
	 
	 public void setTextProvider(final TextProvider textProvider)
	 {
	    this.textProvider = textProvider;
	 }  

}
