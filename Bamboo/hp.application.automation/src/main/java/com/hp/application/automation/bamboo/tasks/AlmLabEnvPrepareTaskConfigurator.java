package com.hp.application.automation.bamboo.tasks;

import com.atlassian.bamboo.collections.ActionParametersMap;
import com.atlassian.bamboo.task.AbstractTaskConfigurator;
import com.atlassian.bamboo.task.TaskDefinition;
import com.atlassian.bamboo.utils.error.ErrorCollection;
import com.atlassian.bamboo.utils.i18n.I18nBean;
import com.atlassian.bamboo.ww2.actions.build.admin.create.UIConfigSupport;
import com.atlassian.util.concurrent.NotNull;
import com.atlassian.util.concurrent.Nullable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang.StringUtils;

public class AlmLabEnvPrepareTaskConfigurator extends AbstractTaskConfigurator {

	private UIConfigSupport uiConfigBean;
	public static final String UI_CONFIG_BEAN_PARAM = "uiConfigBean";

	public static final String ALM_SERVER = "almServer";
	public static final String USER_NAME = "userName";
	public static final String PASSWORD = "password";
	public static final String DOMAIN = "domain";
	public static final String PROJECT = "project";

	public static final String AUT_ENV_ID = "AUTEnvID";
	public static final String PATH_TO_JSON_FILE = "pathToJSONFile";
	public static final String ASSIGN_ENV_CONF_ID = "assignAUTEnvConfIDto";
//	public static final String ENV_CONFIG = "envConfig";

	public static final String ENV_ALM_CONFIG_PATTERN_OPTION_NEW = "ALMConfUseNew";
	public static final String ENV_ALM_CONFIG_PATTERN_OPTION_EXIST = "ALMConfUseExist";
	public static final String ENV_ALM_CONFIGS = "ALMConfigOptions";
	public static final String ENV_CONF_VALUE = "envConfValue";

	public static final Map ENV_ALM_CONFIG_OPTIONS = new HashMap();
	public static final String ENV_ALM_PARAMETERS_TYPE = "ALMParamType";
	public static final String ENV_ALM_PARAMETERS_NAME = "almParamName";
	public static final String ENV_ALM_PARAMETERS_VALUE = "almParamValue";
	public static final String ENV_ALM_PARAMETERS_ONLYFIRST = "almParamOnlyFirst";

	public static final String ENV_ALM_PARAMETERS_TYPE_ENV = "ALMParamTypeEnv";
	public static final String ENV_ALM_PARAMETERS_TYPE_JSON = "ALMParamTypeJson";
	public static final String ENV_ALM_PARAMETERS_TYPE_MAN = "ALMParamTypeManual";
	
	public static final List<AlmConfigureParameter> almParams = new ArrayList();
	
	public Map<String, String> generateTaskConfigMap(@NotNull ActionParametersMap params, @Nullable TaskDefinition previousTaskDefinition) {
		Map config = super.generateTaskConfigMap(params, previousTaskDefinition);
		config.put(ALM_SERVER, params.getString(ALM_SERVER));
		config.put(USER_NAME, params.getString(USER_NAME));
		config.put(PASSWORD, params.getString(PASSWORD));
		config.put(DOMAIN, params.getString(DOMAIN));
		config.put(PROJECT, params.getString(PROJECT));

		config.put(ENV_ALM_CONFIGS, params.getString(ENV_ALM_CONFIGS));
		config.put(ENV_CONF_VALUE, params.getString(ENV_CONF_VALUE));
		config.put(AUT_ENV_ID, params.getString(AUT_ENV_ID));
		config.put(PATH_TO_JSON_FILE, params.getString(PATH_TO_JSON_FILE));
		config.put(ASSIGN_ENV_CONF_ID, params.getString(ASSIGN_ENV_CONF_ID));

		config.put(ENV_ALM_PARAMETERS_TYPE, params.getString(ENV_ALM_PARAMETERS_TYPE));
		config.put(ENV_ALM_PARAMETERS_NAME, params.getString(ENV_ALM_PARAMETERS_NAME));
		config.put(ENV_ALM_PARAMETERS_VALUE, params.getString(ENV_ALM_PARAMETERS_VALUE));
		config.put(ENV_ALM_PARAMETERS_ONLYFIRST, params.getString(ENV_ALM_PARAMETERS_ONLYFIRST));

		return config;
	}

	public void validate(@NotNull final ActionParametersMap params, @NotNull final ErrorCollection errorCollection) {
		super.validate(params, errorCollection);

		I18nBean textProvider = getI18nBean();

		if (StringUtils.isEmpty(params.getString(ALM_SERVER))) {
			errorCollection.addError(ALM_SERVER, textProvider.getText("AlmLabEnvPrepareTask.error.almServerIsEmpty"));
		}

		if (StringUtils.isEmpty(params.getString(USER_NAME))) {
			errorCollection.addError(USER_NAME, textProvider.getText("AlmLabEnvPrepareTask.error.userNameIsEmpty"));
		}
		if (StringUtils.isEmpty(params.getString(DOMAIN))) {
			errorCollection.addError(DOMAIN, textProvider.getText("AlmLabEnvPrepareTask.error.domainIsEmpty"));
		}
		if (StringUtils.isEmpty(params.getString(PROJECT))) {
			errorCollection.addError(PROJECT, textProvider.getText("AlmLabEnvPrepareTask.error.projectIsEmpty"));
		}


		if(params.getString(ENV_ALM_CONFIGS).equals(ENV_ALM_CONFIG_PATTERN_OPTION_NEW) && StringUtils.isEmpty(params.getString(ENV_CONF_VALUE))) {
			errorCollection.addError(ENV_CONF_VALUE, textProvider.getText("AlmLabEnvPrepareTask.error.assignAUTEnvConfValueIsNotAssigned"));
		}

	}

	@Override
	public void populateContextForCreate(@NotNull final Map<String, Object> context) {

		super.populateContextForCreate(context);
		this.populateContextForLists(context);

		context.put(ENV_ALM_CONFIGS, ENV_ALM_CONFIG_PATTERN_OPTION_EXIST);
	}

	@Override
	public void populateContextForEdit(@NotNull final Map<String, Object> context,
			@NotNull final TaskDefinition taskDefinition) {
		super.populateContextForEdit(context, taskDefinition);

		populateContext(context, taskDefinition);
		populateContextForLists(context);
	}

	private void populateContextForLists(@NotNull final Map<String, Object> context) {
		context.put(UI_CONFIG_BEAN_PARAM, uiConfigBean);

		I18nBean textProvider = getI18nBean();

		if(ENV_ALM_CONFIG_OPTIONS.isEmpty()) {
			ENV_ALM_CONFIG_OPTIONS.put(ENV_ALM_CONFIG_PATTERN_OPTION_EXIST, textProvider.getText("AlmLabEnvPrepareTask.useAnExistingConfInputLbl"));
			ENV_ALM_CONFIG_OPTIONS.put(ENV_ALM_CONFIG_PATTERN_OPTION_NEW, textProvider.getText("AlmLabEnvPrepareTask.createNewConfInputLbl"));
		}
		context.put("ALMConfigOptionsMap", ENV_ALM_CONFIG_OPTIONS);

		HashMap paramTypes = new HashMap();
		paramTypes.put(ENV_ALM_PARAMETERS_TYPE_MAN, textProvider.getText("AlmLabEnvPrepareTask.Parameter.Type.Manual"));
		paramTypes.put(ENV_ALM_PARAMETERS_TYPE_ENV, textProvider.getText("AlmLabEnvPrepareTask.Parameter.Type.Environment"));
		paramTypes.put(ENV_ALM_PARAMETERS_TYPE_JSON, textProvider.getText("AlmLabEnvPrepareTask.Parameter.Type.FromJSON"));
		context.put("ALMParamsTypes", paramTypes);

		context.put("almParams", almParams);
	}

	private void populateContext(@NotNull final Map<String, Object> context,
			@NotNull final TaskDefinition taskDefinition) {
		context.put(ALM_SERVER, taskDefinition.getConfiguration().get(ALM_SERVER));
		context.put(USER_NAME, taskDefinition.getConfiguration().get(USER_NAME));
		context.put(PASSWORD, taskDefinition.getConfiguration().get(PASSWORD));
		context.put(DOMAIN, taskDefinition.getConfiguration().get(DOMAIN));
		context.put(PROJECT, taskDefinition.getConfiguration().get(PROJECT));

		context.put(ENV_ALM_CONFIGS, taskDefinition.getConfiguration().get(ENV_ALM_CONFIGS));
		context.put(ENV_CONF_VALUE, taskDefinition.getConfiguration().get(ENV_CONF_VALUE));
		context.put(AUT_ENV_ID, taskDefinition.getConfiguration().get(AUT_ENV_ID));
		context.put(PATH_TO_JSON_FILE, taskDefinition.getConfiguration().get(PATH_TO_JSON_FILE));
		context.put(ASSIGN_ENV_CONF_ID, taskDefinition.getConfiguration().get(ASSIGN_ENV_CONF_ID));
	}

	public void setUiConfigBean(final UIConfigSupport uiConfigBean) {
		this.uiConfigBean = uiConfigBean;
	}
}
