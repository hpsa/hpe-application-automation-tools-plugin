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

import com.atlassian.bamboo.collections.ActionParametersMap;
import com.atlassian.bamboo.task.AbstractTaskConfigurator;
import com.atlassian.bamboo.task.TaskDefinition;
import com.atlassian.bamboo.utils.error.ErrorCollection;
import com.atlassian.bamboo.utils.i18n.I18nBean;
import com.atlassian.util.concurrent.NotNull;
import com.atlassian.util.concurrent.Nullable;

import java.util.*;

import org.apache.commons.lang.StringUtils;

public class AlmLabEnvPrepareTaskConfigurator extends AbstractTaskConfigurator {

	//shared constants
	public static final String ALM_SERVER = "almServer";
	public static final String USER_NAME = "almUserName";
	public static final String PASSWORD = "almUserPassword";
	public static final String DOMAIN = "domain";
	public static final String PROJECT = "almProject";

	public static final String AUT_ENV_ID = "AUTEnvID";
	public static final String AUT_ENV_NEW_CONFIG_NAME = "NewAUTConfName";
	public static final String AUT_ENV_EXIST_CONFIG_ID = "AUTConfName";

	public static final String PATH_TO_JSON_FILE = "pathToJSONFile";
	public static final String ASSIGN_ENV_CONF_ID = "assignAUTEnvConfIDto";

	public static final String ENV_ALM_PARAMETERS_TYPE_ENV = "ALMParamTypeEnv";
	public static final String ENV_ALM_PARAMETERS_TYPE_JSON = "ALMParamTypeJson";
	public static final String ENV_ALM_PARAMETERS_TYPE_MAN = "ALMParamTypeManual";

	public static final String  OUTPUT_CONFIGID= "outEnvID";

	//lists and maps for controls with collections
	private static final String ENV_ALM_CONFIGS_OPTION = "ALMConfigOptions";
	private static final String ENV_ALM_CONFIG_PATTERN_OPTION_NEW = "ALMConfUseNew";
	private static final String ENV_ALM_CONFIG_PATTERN_OPTION_EXIST = "ALMConfUseExist";

	private static final Map ENV_ALM_CONFIG_OPTIONS = new HashMap();
	private static final String ENV_ALM_PARAMETERS_TYPE = "almParamTypes";

	private static final String ENV_ALM_PARAMETERS_NAME = "almParamName";
	private static final String ENV_ALM_PARAMETERS_VALUE = "almParamValue";
	private static final String ENV_ALM_PARAMETERS_ONLYFIRST = "almParamOnlyFirst";

	@NotNull
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
		config.put(AUT_ENV_EXIST_CONFIG_ID, params.getString(AUT_ENV_EXIST_CONFIG_ID));
		config.put(AUT_ENV_ID, params.getString(AUT_ENV_ID));
		config.put(PATH_TO_JSON_FILE, params.getString(PATH_TO_JSON_FILE));
		config.put(ASSIGN_ENV_CONF_ID, params.getString(ASSIGN_ENV_CONF_ID));
		config.put(OUTPUT_CONFIGID, params.getString(OUTPUT_CONFIGID));

		//parse params

		String[] typesArr = params.getStringArray(ENV_ALM_PARAMETERS_TYPE);
		String[] namesArr = params.getStringArray(ENV_ALM_PARAMETERS_NAME);
		String[] valuesArr = params.getStringArray(ENV_ALM_PARAMETERS_VALUE);
		String[] chkOnlyFirstArr = params.getStringArray(ENV_ALM_PARAMETERS_ONLYFIRST);

		if(namesArr != null && typesArr != null && valuesArr != null){
			for(int i = 0, chk = 0; i < Math.min(namesArr.length, typesArr.length); ++i) {

				if(StringUtils.isEmpty(namesArr[i]) || StringUtils.isEmpty(valuesArr[i]))
					continue;

				String dlm = "&;";
				String s = typesArr[i] + dlm + namesArr[i] + dlm + valuesArr[i];

				if(typesArr[i].equals(PATH_TO_JSON_FILE) && chkOnlyFirstArr != null && chkOnlyFirstArr.length > 0)
				{
					s += dlm + chkOnlyFirstArr[chk];
					chk++;
				}
				else
					s += dlm + "false";

				config.put("alm_param_" + i, s);
			}
		}

		return config;
	}

	@NotNull
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

		if(ENV_ALM_CONFIG_PATTERN_OPTION_NEW.equals(params.getString(ENV_ALM_CONFIGS_OPTION)))
		{
			if(StringUtils.isEmpty(params.getString(AUT_ENV_NEW_CONFIG_NAME))) {
				errorCollection.addError(AUT_ENV_NEW_CONFIG_NAME, textProvider.getText("AlmLabEnvPrepareTask.error.assignAUTEnvConfValueIsNotAssigned"));
			}
		}else
			{
				if(StringUtils.isEmpty(params.getString(AUT_ENV_EXIST_CONFIG_ID))) {
					errorCollection.addError(AUT_ENV_EXIST_CONFIG_ID, textProvider.getText("AlmLabEnvPrepareTask.error.assignAUTEnvConfValueIsNotAssigned"));
				}
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
		I18nBean textProvider = getI18nBean();

		if(ENV_ALM_CONFIG_OPTIONS.isEmpty()) {
			ENV_ALM_CONFIG_OPTIONS.put(ENV_ALM_CONFIG_PATTERN_OPTION_EXIST, textProvider.getText("AlmLabEnvPrepareTask.useAnExistingConfInputLbl"));
			ENV_ALM_CONFIG_OPTIONS.put(ENV_ALM_CONFIG_PATTERN_OPTION_NEW, textProvider.getText("AlmLabEnvPrepareTask.createNewConfInputLbl"));
		}
		context.put("ALMConfigOptionsMap", ENV_ALM_CONFIG_OPTIONS);

		Map<String, String> paramTypes = new HashMap<String, String>();
		paramTypes.put(ENV_ALM_PARAMETERS_TYPE_ENV, textProvider.getText("AlmLabEnvPrepareTask.Parameter.Type.Environment"));
		paramTypes.put(ENV_ALM_PARAMETERS_TYPE_JSON, textProvider.getText("AlmLabEnvPrepareTask.Parameter.Type.FromJSON"));
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
		context.put(AUT_ENV_EXIST_CONFIG_ID, configuration.get(AUT_ENV_EXIST_CONFIG_ID));
		context.put(AUT_ENV_ID, configuration.get(AUT_ENV_ID));

		context.put(PATH_TO_JSON_FILE, configuration.get(PATH_TO_JSON_FILE));
		context.put(ASSIGN_ENV_CONF_ID, configuration.get(ASSIGN_ENV_CONF_ID));
		context.put(OUTPUT_CONFIGID, configuration.get(OUTPUT_CONFIGID));

		List<AlmConfigParameter> almParams = fetchAlmParametersFromContext(configuration);
		context.put("almParams", almParams);
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
