package com.hp.application.automation.bamboo.tasks;

import java.util.HashMap;
import java.util.Map;
import com.atlassian.bamboo.collections.ActionParametersMap;
import com.atlassian.bamboo.task.AbstractTaskConfigurator;
import com.atlassian.bamboo.task.TaskDefinition;
import com.atlassian.bamboo.utils.error.ErrorCollection;
import com.atlassian.bamboo.utils.i18n.I18nBean;
import com.atlassian.bamboo.ww2.actions.build.admin.create.UIConfigSupport;
import com.atlassian.util.concurrent.NotNull;
import com.atlassian.util.concurrent.Nullable;
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
	public static final String ENV_CONFIG = "envConfig";
	public static final String ENV_CONF_ITEMS = "envConfigItems";
	public static final String ENV_CONF_VALUE = "envConfValue";
	public static final String NONE_INDEX = "1";

	public Map<String, String> generateTaskConfigMap(@NotNull final ActionParametersMap params,
			@Nullable final TaskDefinition previousTaskDefinition) {
		final Map<String, String> config = super.generateTaskConfigMap(params, previousTaskDefinition);

		config.put(ALM_SERVER, params.getString(ALM_SERVER));
		config.put(USER_NAME, params.getString(USER_NAME));
		config.put(PASSWORD, params.getString(PASSWORD));
		config.put(DOMAIN, params.getString(DOMAIN));
		config.put(PROJECT, params.getString(PROJECT));
		config.put(PATH_TO_JSON_FILE, params.getString(PATH_TO_JSON_FILE));
		config.put(ASSIGN_ENV_CONF_ID, params.getString(ASSIGN_ENV_CONF_ID));
		config.put(ENV_CONFIG, params.getString(ENV_CONFIG));
		config.put(ENV_CONF_VALUE, params.getString(ENV_CONF_VALUE));

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
		if (StringUtils.isEmpty(params.getString(AUT_ENV_ID))) {
			errorCollection.addError(AUT_ENV_ID, textProvider.getText("AlmLabEnvPrepareTask.error.AUTEnvIDIsEmpty"));
		}
		if (!StringUtils.equals(params.getString(ENV_CONFIG), NONE_INDEX)
				&& StringUtils.isEmpty(params.getString(ENV_CONF_VALUE))) {
			errorCollection.addError(ENV_CONF_VALUE,
					textProvider.getText("AlmLabEnvPrepareTask.error.assignAUTEnvConfValueIsNotAssigned"));
		}
	}

	@Override
	public void populateContextForCreate(@NotNull final Map<String, Object> context) {
		super.populateContextForCreate(context);

		populateContextForLists(context);
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

		Map<String, String> runTypesMap = new HashMap<String, String>();
		runTypesMap.put(NONE_INDEX, textProvider.getText("AlmLabEnvPrepareTask.noneConfigInputLbl"));
		runTypesMap.put("2", textProvider.getText("AlmLabEnvPrepareTask.createNewConfInputLbl"));
		runTypesMap.put("3", textProvider.getText("AlmLabEnvPrepareTask.useAnExistingConfInputLbl"));

		context.put(ENV_CONF_ITEMS, runTypesMap);
	}

	private void populateContext(@NotNull final Map<String, Object> context,
			@NotNull final TaskDefinition taskDefinition) {
		context.put(ALM_SERVER, taskDefinition.getConfiguration().get(ALM_SERVER));
		context.put(USER_NAME, taskDefinition.getConfiguration().get(USER_NAME));
		context.put(PASSWORD, taskDefinition.getConfiguration().get(PASSWORD));
		context.put(DOMAIN, taskDefinition.getConfiguration().get(DOMAIN));
		context.put(PROJECT, taskDefinition.getConfiguration().get(PROJECT));
		context.put(PATH_TO_JSON_FILE, taskDefinition.getConfiguration().get(PATH_TO_JSON_FILE));
		context.put(ASSIGN_ENV_CONF_ID, taskDefinition.getConfiguration().get(ASSIGN_ENV_CONF_ID));
		context.put(ENV_CONFIG, taskDefinition.getConfiguration().get(ENV_CONFIG));
		context.put(ENV_CONF_VALUE, taskDefinition.getConfiguration().get(ENV_CONF_VALUE));
	}

	public void setUiConfigBean(final UIConfigSupport uiConfigBean) {
		this.uiConfigBean = uiConfigBean;
	}
}
