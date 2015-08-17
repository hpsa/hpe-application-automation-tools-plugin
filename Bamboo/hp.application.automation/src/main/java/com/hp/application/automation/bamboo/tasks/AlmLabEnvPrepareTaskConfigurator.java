package com.hp.application.automation.bamboo.tasks;

import com.atlassian.bamboo.collections.ActionParametersMap;
import com.atlassian.bamboo.task.AbstractTaskConfigurator;
import com.atlassian.bamboo.task.TaskDefinition;
import com.atlassian.bamboo.utils.error.ErrorCollection;
import com.atlassian.bamboo.utils.i18n.I18nBean;
import com.atlassian.bamboo.ww2.actions.build.admin.create.UIConfigSupport;
import com.atlassian.util.concurrent.NotNull;
import com.atlassian.util.concurrent.Nullable;

import java.util.*;

import org.apache.commons.lang.StringUtils;

public class AlmLabEnvPrepareTaskConfigurator extends AbstractTaskConfigurator {

	private UIConfigSupport uiConfigBean;
	private static final String UI_CONFIG_BEAN_PARAM = "uiConfigBean";

	//shared constants
	public static final String ALM_SERVER = "almServer";
	public static final String USER_NAME = "almUserName";
	public static final String PASSWORD = "almUserPassword";
	public static final String DOMAIN = "domain";
	public static final String PROJECT = "almProject";

	public static final String AUT_ENV_ID = "AUTEnvID";
	public static final String AUT_ENV_NEW_CONFIG_NAME = "AUTConfName";

	public static final String PATH_TO_JSON_FILE = "pathToJSONFile";
	public static final String ASSIGN_ENV_CONF_ID = "assignAUTEnvConfIDto";

	public static final String ENV_ALM_PARAMETERS_TYPE_ENV = "ALMParamTypeEnv";
	public static final String ENV_ALM_PARAMETERS_TYPE_JSON = "ALMParamTypeJson";
	public static final String ENV_ALM_PARAMETERS_TYPE_MAN = "ALMParamTypeManual";

	//lists and maps for contrals with collections
	private static final String ENV_ALM_CONFIGS_OPTION = "ALMConfigOptions";
	private static final String ENV_ALM_CONFIG_PATTERN_OPTION_NEW = "ALMConfUseNew";
	private static final String ENV_ALM_CONFIG_PATTERN_OPTION_EXIST = "ALMConfUseExist";

	private static final Map ENV_ALM_CONFIG_OPTIONS = new HashMap();
	private static final String ENV_ALM_PARAMETERS_TYPE = "almParamTypes";

	private static final String ENV_ALM_PARAMETERS_NAME = "almParamName";
	private static final String ENV_ALM_PARAMETERS_VALUE = "almParamValue";
	private static final String ENV_ALM_PARAMETERS_ONLYFIRST = "almParamOnlyFirst";

	private static final List<AlmConfigParameter> almParams = new ArrayList();

	private static  final String testVal = "lova you";

	public AlmLabEnvPrepareTaskConfigurator() {
		System.out.print("con");
	}

	public Map<String, String> generateTaskConfigMap(@NotNull final ActionParametersMap params,
			@Nullable final TaskDefinition previousTaskDefinition) {
		final Map<String, String> config = super.generateTaskConfigMap(params, previousTaskDefinition);

		config.put(ALM_SERVER, params.getString(ALM_SERVER));
		config.put(USER_NAME, params.getString(USER_NAME));
		config.put(PASSWORD, params.getString(PASSWORD));
		config.put(DOMAIN, params.getString(DOMAIN));
		config.put(PROJECT, params.getString(PROJECT));

		config.put(ENV_ALM_CONFIGS_OPTION, params.getString(ENV_ALM_CONFIGS_OPTION));
		config.put(AUT_ENV_NEW_CONFIG_NAME, params.getString(AUT_ENV_NEW_CONFIG_NAME));
		config.put(AUT_ENV_ID, params.getString(AUT_ENV_ID));
		config.put(PATH_TO_JSON_FILE, params.getString(PATH_TO_JSON_FILE));
		config.put(ASSIGN_ENV_CONF_ID, params.getString(ASSIGN_ENV_CONF_ID));

		//parse params

		String[] typesArr = params.getStringArray(ENV_ALM_PARAMETERS_TYPE);
		String[] namesArr = params.getStringArray(ENV_ALM_PARAMETERS_NAME);
		String[] valuesArr = params.getStringArray(ENV_ALM_PARAMETERS_VALUE);
		String[] chkOnlyFirstArr = params.getStringArray(ENV_ALM_PARAMETERS_ONLYFIRST);

		int countNumber = namesArr.length;

		for(int i=0, chk=0; i<countNumber; ++i) {

			if(StringUtils.isEmpty(namesArr[i]) || StringUtils.isEmpty(valuesArr[i]))
				continue;

			StringJoiner sj = new StringJoiner("&;");
			sj.add(typesArr[i]).add(namesArr[i]).add(valuesArr[i]);
			if(typesArr[i].equals(PATH_TO_JSON_FILE))
			{
				sj.add(chkOnlyFirstArr[chk]);
				chk++;
			}
			else
				sj.add("false");

			config.put("alm_param_" + i, sj.toString());
		}

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

		if(params.getString(ENV_ALM_CONFIGS_OPTION).equals(ENV_ALM_CONFIG_PATTERN_OPTION_NEW) &&
									StringUtils.isEmpty(params.getString(AUT_ENV_NEW_CONFIG_NAME))) {
			errorCollection.addError(AUT_ENV_NEW_CONFIG_NAME, textProvider.getText("AlmLabEnvPrepareTask.error.assignAUTEnvConfValueIsNotAssigned"));
		}
	}

	@Override
	public void populateContextForCreate(@NotNull final Map<String, Object> context) {

		super.populateContextForCreate(context);
		this.populateContextForLists(context);

		context.put(ENV_ALM_CONFIGS_OPTION, ENV_ALM_CONFIG_PATTERN_OPTION_EXIST);
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

		Map<String, String> paramTypes = new HashMap<String, String>();
		paramTypes.put(ENV_ALM_PARAMETERS_TYPE_JSON, textProvider.getText("AlmLabEnvPrepareTask.Parameter.Type.FromJSON"));
		paramTypes.put(ENV_ALM_PARAMETERS_TYPE_ENV, textProvider.getText("AlmLabEnvPrepareTask.Parameter.Type.Environment"));
		paramTypes.put(ENV_ALM_PARAMETERS_TYPE_MAN, textProvider.getText("AlmLabEnvPrepareTask.Parameter.Type.Manual"));

		context.put("ALMParamsTypes", paramTypes);
	}

	private void populateContext(@NotNull final Map<String, Object> context,
			@NotNull final TaskDefinition taskDefinition) {

		final Map<String, String> configuration = taskDefinition.getConfiguration();

		context.put(ALM_SERVER, configuration.get(ALM_SERVER));
		context.put(USER_NAME, configuration.get(USER_NAME));
		context.put(PASSWORD, configuration.get(PASSWORD));
		context.put(DOMAIN, configuration.get(DOMAIN));
		context.put(PROJECT, configuration.get(PROJECT));

		context.put(ENV_ALM_CONFIGS_OPTION, configuration.get(ENV_ALM_CONFIGS_OPTION));
		context.put(AUT_ENV_NEW_CONFIG_NAME, configuration.get(AUT_ENV_NEW_CONFIG_NAME));
		context.put(AUT_ENV_ID, configuration.get(AUT_ENV_ID));

		context.put(PATH_TO_JSON_FILE, configuration.get(PATH_TO_JSON_FILE));
		context.put(ASSIGN_ENV_CONF_ID, configuration.get(ASSIGN_ENV_CONF_ID));

		List<AlmConfigParameter> almParams = fetchAlmParametersFromContext(configuration);
		context.put("almParams", almParams);
	}

	public void setUiConfigBean(final UIConfigSupport uiConfigBean) {
		this.uiConfigBean = uiConfigBean;
	}

	public static List<AlmConfigParameter> fetchAlmParametersFromContext(@NotNull final Map<String, String> context) {

		List<AlmConfigParameter> almParams = new ArrayList<AlmConfigParameter>(context.size());

		for(String key: context.keySet())
		{
			if(key.startsWith("alm_param_"))
			{
				String[] arr = context.get(key).split("&;");
				almParams.add(new AlmConfigParameter(arr[0], arr[1], arr[2], arr[3]));
			}
		}

		return almParams;
	}

	public static boolean useExistingConfiguration(Map<String, String> confMap) {

		return confMap.get(ENV_ALM_CONFIGS_OPTION).equals(ENV_ALM_CONFIG_PATTERN_OPTION_EXIST);
	}
}
