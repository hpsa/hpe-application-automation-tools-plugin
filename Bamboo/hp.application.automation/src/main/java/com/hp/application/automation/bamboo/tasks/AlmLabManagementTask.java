package com.hp.application.automation.bamboo.tasks;

import com.atlassian.bamboo.build.logger.BuildLogger;
import com.atlassian.bamboo.build.test.TestCollationService;
import com.atlassian.bamboo.build.test.TestReportProvider;
import com.atlassian.bamboo.configuration.ConfigurationMap;
import com.atlassian.bamboo.task.*;
import com.atlassian.bamboo.v2.build.agent.capability.CapabilityContext;
import com.hp.application.automation.tools.common.model.CdaDetails;
import com.hp.application.automation.tools.common.model.SseModel;
import com.hp.application.automation.tools.common.rest.RestClient;
import com.hp.application.automation.tools.common.result.model.junit.Testsuites;
import com.hp.application.automation.tools.common.sdk.Args;
import com.hp.application.automation.tools.common.sdk.ConsoleLogger;
import com.hp.application.automation.tools.common.sdk.Logger;
import com.hp.application.automation.tools.common.sdk.RunManager;
import org.apache.commons.lang.BooleanUtils;
import org.jetbrains.annotations.NotNull;

import java.util.Properties;

public class AlmLabManagementTask implements TaskType {

	private final TestCollationService _testCollationService;
	private final CapabilityContext _capabilityContext;
	
	public AlmLabManagementTask(TestCollationService testCollationService, CapabilityContext capabilityContext){
		_testCollationService = testCollationService;
		_capabilityContext = capabilityContext;
	}

    @NotNull
    @java.lang.Override
    public TaskResult execute(@NotNull final TaskContext taskContext) throws TaskException
    {
        final BuildLogger buildLogger = taskContext.getBuildLogger();

        final ConfigurationMap map = taskContext.getConfigurationMap();
        
        final String almServer = map.get(AlmLabManagementTaskConfigurator.ALM_SERVER_PARAM);
        final String almServerPath = _capabilityContext.getCapabilityValue(AlmServerCapabilityHelper.GetCapabilityKey(almServer));

        RunManager runManager = new RunManager();

        CdaDetails cdaDetails = null;
        boolean useCda = BooleanUtils.toBoolean(map.get(AlmLabManagementTaskConfigurator.USE_SDA_PARAM));
        if(useCda)
        {
            cdaDetails = new CdaDetails(map.get(AlmLabManagementTaskConfigurator.DEPLOYMENT_ACTION_PARAM),
                                        map.get(AlmLabManagementTaskConfigurator.DEPOYED_ENVIROMENT_NAME_PARAM),
                                        map.get(AlmLabManagementTaskConfigurator.DEPROVISIONING_ACTION_PARAM));
        }

        Args args = new Args(
                almServerPath,
                map.get(AlmLabManagementTaskConfigurator.DOMAIN_PARAM),
                map.get(AlmLabManagementTaskConfigurator.PROJECT_NAME_PARAM),
                map.get(AlmLabManagementTaskConfigurator.USER_NAME_PARAM),
                map.get(AlmLabManagementTaskConfigurator.PASSWORD_PARAM),
                map.get(AlmLabManagementTaskConfigurator.RUN_TYPE_PARAM),
                map.get(AlmLabManagementTaskConfigurator.TEST_ID_PARAM),
                map.get(AlmLabManagementTaskConfigurator.DURATION_PARAM),
                map.get(AlmLabManagementTaskConfigurator.DESCRIPTION_PARAM),
                null,
                map.get(AlmLabManagementTaskConfigurator.ENVIROMENT_ID_PARAM),
                cdaDetails);

        RestClient restClient =
                new RestClient(
                        args.getUrl(),
                        args.getDomain(),
                        args.getProject(),
                        args.getUsername());

        try
        {
            Logger logger = new Logger() {

                public void log(String message) {
                    buildLogger.addBuildLogEntry(message);
                }
            };

            Testsuites ret = runManager.execute(restClient, args, logger);

        }
        catch (InterruptedException e)
        {
            e.printStackTrace();
        }


        //final String testFilePattern = "*.txt";         
        //_testCollationService.collateTestResults(taskContext, testFilePattern, new TestResultsReportCollector(), true);

        return TaskResultBuilder.create(taskContext).checkTestFailures().build();
    }
}
