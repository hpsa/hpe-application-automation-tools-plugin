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
package com.hp.application.automation.bamboo.tasks;

import java.lang.String;
import java.util.ArrayList;
import java.util.List;

import com.atlassian.bamboo.build.logger.BuildLogger;
import com.atlassian.bamboo.task.*;
import com.atlassian.bamboo.variable.VariableDefinitionManager;
import com.hp.application.automation.tools.common.StringUtils;
import com.hp.application.automation.tools.common.model.AutEnvironmentConfigModel;
import com.hp.application.automation.tools.common.model.AutEnvironmentParameterModel;
import com.hp.application.automation.tools.common.model.AutEnvironmentParameterType;
import com.hp.application.automation.tools.common.rest.RestClient;
import com.hp.application.automation.tools.common.sdk.Logger;
import com.hp.application.automation.tools.common.sdk.AUTEnvironmentBuilderPerformer;
import org.jetbrains.annotations.NotNull;
import com.atlassian.bamboo.configuration.ConfigurationMap;

public class AlmLabEnvPrepareTask implements TaskType {

	private final VariableService variableService;

	public AlmLabEnvPrepareTask(VariableDefinitionManager variableDefinitionManager)
	{
		this.variableService = new VariableService(variableDefinitionManager);
	}


	@NotNull
	public TaskResult execute(@NotNull TaskContext taskContext) throws TaskException {

		final BuildLogger buildLogger = taskContext.getBuildLogger();
		ConfigurationMap confMap = taskContext.getConfigurationMap();
		TaskState state = TaskState.SUCCESS;

		String domain = confMap.get(AlmLabEnvPrepareTaskConfigurator.DOMAIN);
		String project = confMap.get(AlmLabEnvPrepareTaskConfigurator.PROJECT);
		String userName = confMap.get(AlmLabEnvPrepareTaskConfigurator.USER_NAME);

		boolean useExistingAutEnvConf = AlmLabEnvPrepareTaskConfigurator.useExistingConfiguration(confMap);
		String configuration = useExistingAutEnvConf ?
								confMap.get(AlmLabEnvPrepareTaskConfigurator.AUT_ENV_EXIST_CONFIG_ID):
								confMap.get(AlmLabEnvPrepareTaskConfigurator.AUT_ENV_NEW_CONFIG_NAME);

		List<AutEnvironmentParameterModel> autEnvironmentParameters = new ArrayList<AutEnvironmentParameterModel>();
		for(AlmConfigParameter prm: AlmLabEnvPrepareTaskConfigurator.fetchAlmParametersFromContext(confMap))
		{
			AutEnvironmentParameterType type = convertType(prm.getAlmParamSourceType());

			autEnvironmentParameters.add(
					new AutEnvironmentParameterModel(prm.getAlmParamName(),
							prm.getAlmParamValue(),
							type,
							prm.getAlmParamOnlyFirst()));
		}

		String almServerPath = confMap.get(AlmLabEnvPrepareTaskConfigurator.ALM_SERVER);

		RestClient restClient = new RestClient(
				almServerPath,
				domain,
				project,
				userName);


		AutEnvironmentConfigModel autEnvModel = new AutEnvironmentConfigModel(
				almServerPath,
				userName,
				confMap.get(AlmLabEnvPrepareTaskConfigurator.PASSWORD),
				domain,
				project,
				useExistingAutEnvConf,
				confMap.get(AlmLabEnvPrepareTaskConfigurator.AUT_ENV_ID),
				configuration,
				confMap.get(AlmLabEnvPrepareTaskConfigurator.PATH_TO_JSON_FILE),
				autEnvironmentParameters);

		try {

				Logger logger = new Logger() {

				public void log(String message) {
					buildLogger.addBuildLogEntry(message);
				}
			};

			AUTEnvironmentBuilderPerformer performer = new AUTEnvironmentBuilderPerformer(restClient, logger, autEnvModel);
			performer.start();

			String outputConfig = confMap.get(AlmLabEnvPrepareTaskConfigurator.OUTPUT_CONFIGID);

			if (!StringUtils.isNullOrEmpty(outputConfig)) {

				String confId = autEnvModel.getCurrentConfigID();
				variableService.saveGlobalVariable(outputConfig, confId);
			}

		} catch (InterruptedException e) {
			state = TaskState.ERROR;
		} catch (Throwable cause) {
			state = TaskState.FAILED;
		}

		TaskResultBuilder result = TaskResultBuilder.create(taskContext);
		result.setState(state);

		return result.build();
	}

	private AutEnvironmentParameterType convertType(String sourceType) {

		if(sourceType.equals(AlmLabEnvPrepareTaskConfigurator.ENV_ALM_PARAMETERS_TYPE_ENV))
				return AutEnvironmentParameterType.ENVIRONMENT;

		if(sourceType.equals(AlmLabEnvPrepareTaskConfigurator.ENV_ALM_PARAMETERS_TYPE_JSON))
				return AutEnvironmentParameterType.EXTERNAL;

		if(sourceType.equals(AlmLabEnvPrepareTaskConfigurator.ENV_ALM_PARAMETERS_TYPE_MAN))
				return AutEnvironmentParameterType.USER_DEFINED;

		return AutEnvironmentParameterType.UNDEFINED;
	}

}
