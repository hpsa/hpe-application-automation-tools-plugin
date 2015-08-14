package com.hp.application.automation.bamboo.tasks;

import java.lang.String;
import java.util.ArrayList;
import java.util.List;

import com.atlassian.bamboo.build.logger.BuildLogger;
import com.atlassian.bamboo.build.test.TestCollationService;
import com.atlassian.bamboo.task.TaskContext;
import com.atlassian.bamboo.task.TaskException;
import com.atlassian.bamboo.task.TaskResult;
import com.atlassian.bamboo.task.TaskResultBuilder;
import com.atlassian.bamboo.task.TaskType;
import com.atlassian.bamboo.v2.build.agent.capability.CapabilityContext;
import com.hp.application.automation.tools.common.model.AutEnvironmentConfigModel;
import com.hp.application.automation.tools.common.model.AutEnvironmentParameterModel;
import com.hp.application.automation.tools.common.rest.RestClient;
import com.hp.application.automation.tools.common.sdk.Logger;
import com.hp.application.automation.tools.common.sdk.AUTEnvironmentBuilderPerformer;
import org.jetbrains.annotations.NotNull;
import com.atlassian.bamboo.configuration.ConfigurationMap;

public class AlmLabEnvPrepareTask implements TaskType {

	private final TestCollationService _testCollationService;
	private final CapabilityContext _capabilityContext;

	public AlmLabEnvPrepareTask(@NotNull final TestCollationService testCollationService, CapabilityContext capabilityContext)	{
		this._testCollationService = testCollationService;
		this._capabilityContext = capabilityContext;
	}

	@NotNull
	public TaskResult execute(@NotNull TaskContext taskContext) throws TaskException {

		final BuildLogger buildLogger = taskContext.getBuildLogger();
		ConfigurationMap confMap = taskContext.getConfigurationMap();

		String almServer = confMap.get(AlmLabEnvPrepareTaskConfigurator.ALM_SERVER);
		String domain = confMap.get(AlmLabEnvPrepareTaskConfigurator.DOMAIN);
		String project = confMap.get(AlmLabEnvPrepareTaskConfigurator.PROJECT);
		String userName = confMap.get(AlmLabEnvPrepareTaskConfigurator.USER_NAME);

		boolean useExistingAutEnvConf = AlmLabEnvPrepareTaskConfigurator.useExistingConfiguration(confMap);

		List<AutEnvironmentParameterModel> autEnvironmentParameters = new ArrayList<AutEnvironmentParameterModel>();
		for(AlmConfigParameter prm: AlmLabEnvPrepareTaskConfigurator.fetchAlmParametersFromContext(confMap))
		{
			autEnvironmentParameters.add(
					new AutEnvironmentParameterModel(prm.getAlmParamName(),
							prm.getAlmParamValue(),
							prm.getAlmParamSourceType(),
							prm.getAlmParamOnlyFirst()));
		}

		String almServerPath = this._capabilityContext.getCapabilityValue(AlmServerCapabilityHelper.GetCapabilityKey(almServer));

		RestClient restClient = new RestClient(
				almServerPath,
				domain,
				project,
				userName);

		AutEnvironmentConfigModel autEnvModel = new AutEnvironmentConfigModel(
				almServerPath,
				almServer,
				userName,
				confMap.get(AlmLabEnvPrepareTaskConfigurator.PASSWORD),
				domain,
				project,
				useExistingAutEnvConf,
				confMap.get(AlmLabEnvPrepareTaskConfigurator.AUT_ENV_ID),
				confMap.get(AlmLabEnvPrepareTaskConfigurator.AUT_ENV_NEW_CONFIG_NAME),
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


		} catch (InterruptedException e) {
			//build.setResult(Result.ABORTED);
			//throw e;
		} catch (Throwable cause) {
			//build.setResult(Result.FAILURE);
		}

		return TaskResultBuilder.create(taskContext).checkTestFailures().build();
	}

}
