package com.hp.application.automation.bamboo.tasks;

import com.atlassian.bamboo.build.test.TestCollationService;
import com.atlassian.bamboo.task.*;
import com.atlassian.bamboo.build.logger.BuildLogger;
import com.atlassian.bamboo.configuration.ConfigurationMap;
import java.util.*;
import com.atlassian.bamboo.utils.i18n.I18nBean;
import com.atlassian.bamboo.utils.i18n.I18nBeanFactory;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class RunFromFileSystemTask extends AbstractLauncherTask {

	private I18nBean i18nBean;

	public RunFromFileSystemTask(@NotNull final TestCollationService testCollationService, @NotNull I18nBeanFactory i18nBeanFactory)
	{
		super(testCollationService);
		i18nBean = i18nBeanFactory.getI18nBean();
	}

    @java.lang.Override
	protected Properties getTaskProperties(final TaskContext taskContext) throws Exception {
    	final ConfigurationMap map = taskContext.getConfigurationMap();        
    	LauncherParamsBuilder builder = new LauncherParamsBuilder(); 
   	
    	builder.setRunType(RunType.FileSystem);
        String timeout = map.get(RunFromFileSystemTaskConfigurator.TIMEOUT);
        builder.setPerScenarioTimeOut(timeout);

    	String splitMarker = "\n";
    	String tests = map.get(RunFromFileSystemTaskConfigurator.TESTS_PATH);
    	String[] testNames;
    	if(tests == null)
    	{
    		testNames = new String[0];
    	}
    	else
    	{
    		testNames = tests.split(splitMarker);
    	}
        
        for(int i=0; i < testNames.length; i++)
        {
        	builder.setTest(i+1, testNames[i]);
        }
    	return builder.getProperties();
	}

	@Override
	protected void uploadArtifacts(final TaskContext taskContext)
	{
		TestResultHelper.ResultTypeFilter resultsFilter = getResultTypeFilter(taskContext);

		if(resultsFilter != null)
		{
			final BuildLogger buildLogger = taskContext.getBuildLogger();
			final String resultNameFormat = i18nBean.getText(RunFromFileSystemTaskConfigurator.ARTIFACT_NAME_FORMAT_STRING);

			Collection<ResultInfoItem> resultsPaths = TestResultHelper.getTestResults(getResultsFile(), resultsFilter, resultNameFormat, taskContext, buildLogger);
			TestResultHelper.zipResults(resultsPaths, buildLogger);
		}
	}

	@Nullable
	private TestResultHelper.ResultTypeFilter getResultTypeFilter(final TaskContext taskContext)
	{
		String publishMode = taskContext.getConfigurationMap().get(RunFromFileSystemTaskConfigurator.PUBLISH_MODE_PARAM);

		if(publishMode.equals(RunFromFileSystemTaskConfigurator.PUBLISH_MODE_FAILED_VALUE))
		{
			return TestResultHelper.ResultTypeFilter.FAILED;
		}

		if(publishMode.equals(RunFromFileSystemTaskConfigurator.PUBLISH_MODE_ALWAYS_VALUE))
		{
			return TestResultHelper.ResultTypeFilter.All;
		}

		return null;
	}
}
