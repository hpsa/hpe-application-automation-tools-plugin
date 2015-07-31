package com.hp.application.automation.bamboo.tasks;

import java.util.Properties;

import com.atlassian.bamboo.build.test.TestCollationService;
import com.atlassian.bamboo.task.TaskContext;
import com.atlassian.bamboo.task.TaskException;
import com.atlassian.bamboo.task.TaskResult;
import com.atlassian.bamboo.task.TaskResultBuilder;
import com.atlassian.bamboo.task.TaskType;
import org.jetbrains.annotations.NotNull;
import com.atlassian.bamboo.configuration.ConfigurationMap;

public class AlmLabEnvPrepareTask extends AbstractLauncherTask {
//public class AlmLabEnvPrepareTask implements TaskType {

	public AlmLabEnvPrepareTask(@NotNull final TestCollationService testCollationService)
	{
		super(testCollationService);
	}

    @java.lang.Override
	protected Properties getTaskProperties(final TaskContext taskContext) throws Exception {
    	final ConfigurationMap map = taskContext.getConfigurationMap();        
    	LauncherParamsBuilder builder = new LauncherParamsBuilder();

		builder.setRunType(RunType.Alm);
		builder.setAlmServerUrl(map.get(AlmLabEnvPrepareTaskConfigurator.ALM_SERVER));
		builder.setAlmDomain(map.get(AlmLabEnvPrepareTaskConfigurator.DOMAIN));
		builder.setAlmProject(map.get(AlmLabEnvPrepareTaskConfigurator.PROJECT));
		builder.setAlmUserName(map.get(AlmLabEnvPrepareTaskConfigurator.USER_NAME));
		builder.setAlmPassword(map.get(AlmLabEnvPrepareTaskConfigurator.PASSWORD));
		/*
		TODO: pass those arguments
		builder.setAlmServerUrl(map.get(AlmLabEnvPrepareTaskConfigurator.PATH_TO_JSON_FILE));
		builder.setAlmServerUrl(map.get(AlmLabEnvPrepareTaskConfigurator.ASSIGN_ENV_CONF_ID));
		builder.setAlmServerUrl(map.get(AlmLabEnvPrepareTaskConfigurator.ENV_CONFIG));
		builder.setAlmServerUrl(map.get(AlmLabEnvPrepareTaskConfigurator.ENV_CONF_VALUE));
		*/
    	return builder.getProperties();
	}

}
