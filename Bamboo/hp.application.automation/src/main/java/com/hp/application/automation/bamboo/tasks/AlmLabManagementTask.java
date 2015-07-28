package com.hp.application.automation.bamboo.tasks;

import com.atlassian.bamboo.build.logger.BuildLogger;
import com.atlassian.bamboo.build.test.TestCollationService;
import com.atlassian.bamboo.configuration.ConfigurationMap;
import com.atlassian.bamboo.task.TaskContext;
import com.atlassian.bamboo.task.TaskException;
import com.atlassian.bamboo.task.TaskResult;
import com.atlassian.bamboo.task.TaskResultBuilder;
import com.atlassian.bamboo.task.TaskType;
import com.atlassian.bamboo.v2.build.agent.capability.CapabilityContext;
import com.atlassian.bamboo.v2.build.agent.capability.CapabilityDefaultsHelper;
import org.jetbrains.annotations.NotNull;

import java.util.Properties;

public class AlmLabManagementTask extends AbstractLauncherTask {

	private final TestCollationService _testCollationService;
	private final CapabilityContext _capabilityContext;
	
	public AlmLabManagementTask(TestCollationService testCollationService, CapabilityContext capabilityContext){
		_testCollationService = testCollationService;
		_capabilityContext = capabilityContext;
	}

    @Override
    protected Properties getTaskProperties(TaskContext taskContext) throws Exception {

        final ConfigurationMap map = taskContext.getConfigurationMap();
        LauncherParamsBuilder builder = new LauncherParamsBuilder();

        builder.setRunType(RunType.Alm);

        builder.setAlmServerUrl(map.get(AlmLabManagementTaskConfigurator.ALM_SERVER_PARAM));
        builder.setAlmUserName(map.get(AlmLabManagementTaskConfigurator.USER_NAME_PARAM));
        builder.setAlmPassword(map.get(AlmLabManagementTaskConfigurator.PASSWORD_PARAM));
        builder.setAlmDomain(map.get(AlmLabManagementTaskConfigurator.DOMAIN_PARAM));
        builder.setAlmProject(map.get(AlmLabManagementTaskConfigurator.PROJECT_NAME_PARAM));
        /*
        builder.set(AlmLabManagementTaskConfigurator.TEST_ID_PARAM));
        builder.setAlmServerUrl(map.get(AlmLabManagementTaskConfigurator.DESCRIPTION_PARAM));
        builder.setAlmServerUrl(map.get(AlmLabManagementTaskConfigurator.DURATION_PARAM));
        builder.setAlmServerUrl(map.get(AlmLabManagementTaskConfigurator.ENVIROMENT_ID_PARAM));
        builder.setAlmServerUrl(map.get(AlmLabManagementTaskConfigurator.USE_SDA_PARAM));
        builder.setAlmServerUrl(map.get(AlmLabManagementTaskConfigurator.UI_CONFIG_BEAN_PARAM));
        builder.setAlmServerUrl(map.get(AlmLabManagementTaskConfigurator.RUN_TYPE_ITEMS_PARAM));
        */
        return builder.getProperties();
    }

    /*
    @NotNull
    @java.lang.Override
    public TaskResult execute(@NotNull final TaskContext taskContext) throws TaskException
    {
        final BuildLogger buildLogger = taskContext.getBuildLogger();

        final ConfigurationMap map = taskContext.getConfigurationMap();
        
        final String almServer = map.get(AlmLabManagementTaskConfigurator.ALM_SERVER_PARAM);
        final String almServerPath = _capabilityContext.getCapabilityValue(AlmServerCapabilityHelper.GetCapabilityKey(almServer));
        
        buildLogger.addBuildLogEntry("ALM Server: " + almServer);
        buildLogger.addBuildLogEntry("ALM Server Path: " + almServerPath);
        buildLogger.addBuildLogEntry("User name: " + map.get(AlmLabManagementTaskConfigurator.USER_NAME_PARAM));
        buildLogger.addBuildLogEntry("Password: " + map.get(AlmLabManagementTaskConfigurator.PASSWORD_PARAM));
        buildLogger.addBuildLogEntry("Domain: " + map.get(AlmLabManagementTaskConfigurator.DOMAIN_PARAM));
        buildLogger.addBuildLogEntry("Project: " + map.get(AlmLabManagementTaskConfigurator.PROJECT_NAME_PARAM));
        buildLogger.addBuildLogEntry("Run Type: " + map.get(AlmLabManagementTaskConfigurator.RUN_TYPE_PARAM));
        buildLogger.addBuildLogEntry("Test Set/Build Verification Suite ID: " + map.get(AlmLabManagementTaskConfigurator.TEST_ID_PARAM));
        buildLogger.addBuildLogEntry("Description: " + map.get(AlmLabManagementTaskConfigurator.DESCRIPTION_PARAM));
        buildLogger.addBuildLogEntry("Timeslot Duration: " + map.get(AlmLabManagementTaskConfigurator.DURATION_PARAM));
        buildLogger.addBuildLogEntry("Enviroment Configuration ID: " + map.get(AlmLabManagementTaskConfigurator.ENVIROMENT_ID_PARAM));
        buildLogger.addBuildLogEntry("Use SDA: " + map.get(AlmLabManagementTaskConfigurator.USE_SDA_PARAM));

        //final String testFilePattern = "*.txt";         
        //_testCollationService.collateTestResults(taskContext, testFilePattern, new TestResultsReportCollector(), true);
        
        return TaskResultBuilder.create(taskContext).checkTestFailures().build();
    }
	*/
}
