package com.hpe.application.automation.bamboo.tasks;

import com.atlassian.bamboo.build.logger.BuildLogger;
import com.atlassian.bamboo.build.test.TestCollationService;
import com.atlassian.bamboo.configuration.ConfigurationMap;
import com.atlassian.bamboo.task.*;
import com.atlassian.bamboo.utils.i18n.I18nBean;
import com.atlassian.bamboo.utils.i18n.I18nBeanFactory;
import com.hpe.application.automation.tools.common.SSEException;
import com.hpe.application.automation.tools.common.model.CdaDetails;
import com.hpe.application.automation.tools.common.rest.RestClient;
import com.hpe.application.automation.tools.common.result.ResultSerializer;
import com.hpe.application.automation.tools.common.result.model.junit.Testsuites;
import com.hpe.application.automation.tools.common.sdk.Args;
import com.hpe.application.automation.tools.common.sdk.Logger;
import com.hpe.application.automation.tools.common.sdk.RunManager;
import org.apache.commons.lang.BooleanUtils;
import org.jetbrains.annotations.NotNull;

import static com.hpe.application.automation.bamboo.tasks.TestResultHelperAlm.AddALMArtifacts;

public class AlmLabManagementTask implements TaskType {

	private final TestCollationService testCollationService;
    private final String LINK_SEARCH_FILTER = "run report for run id";
    private static I18nBean i18nBean;
	
	public AlmLabManagementTask(TestCollationService testCollationService, @NotNull I18nBeanFactory i18nBeanFactory){
		this.testCollationService = testCollationService;
        i18nBean = i18nBeanFactory.getI18nBean();
	}

    @NotNull
    @java.lang.Override
    public TaskResult execute(@NotNull final TaskContext taskContext) throws TaskException
    {
        final BuildLogger buildLogger = taskContext.getBuildLogger();

        final ConfigurationMap map = taskContext.getConfigurationMap();
        
        final String almServerPath = map.get(AlmLabManagementTaskConfigurator.ALM_SERVER_PARAM);

        RunManager runManager = new RunManager();

        CdaDetails cdaDetails = null;
        boolean useCda = BooleanUtils.toBoolean(map.get(AlmLabManagementTaskConfigurator.USE_SDA_PARAM));
        if(useCda)
        {
            cdaDetails = new CdaDetails(map.get(AlmLabManagementTaskConfigurator.DEPLOYMENT_ACTION_PARAM),
                                        map.get(AlmLabManagementTaskConfigurator.DEPLOYED_ENVIRONMENT_NAME),
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
                map.get(AlmLabManagementTaskConfigurator.ENVIRONMENT_ID_PARAM),
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

            Testsuites result = runManager.execute(restClient, args, logger);

            ResultSerializer.saveResults(result, taskContext.getWorkingDirectory().getPath(), logger);

        }
        catch (InterruptedException e)
        {
            e.printStackTrace();
            return TaskResultBuilder.create(taskContext).failed().build();
        }
        catch (SSEException e)
        {
            return TaskResultBuilder.create(taskContext).failed().build();
        }

        TestResultHelper.CollateResults(testCollationService, taskContext);
        AddALMArtifacts(taskContext, LINK_SEARCH_FILTER, i18nBean);

        return TaskResultBuilder.create(taskContext).checkTestFailures().build();
    }
}
