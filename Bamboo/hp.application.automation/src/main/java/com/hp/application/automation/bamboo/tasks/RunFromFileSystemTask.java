package com.hp.application.automation.bamboo.tasks;

import com.atlassian.bamboo.build.artifact.ArtifactManager;
import com.atlassian.bamboo.build.test.TestCollationService;
import com.atlassian.bamboo.task.*;
import com.atlassian.bamboo.build.logger.BuildLogger;
import com.atlassian.bamboo.configuration.ConfigurationMap;

import java.util.*;

import com.atlassian.struts.TextProvider;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class RunFromFileSystemTask extends AbstractLauncherTask {

	private final ArtifactManager _artifactManager;
	private final TextProvider _textProvider;

	public RunFromFileSystemTask(@NotNull final TestCollationService testCollationService, @NotNull ArtifactManager artifactManager, @NotNull TextProvider textProvider)
	{
		super(testCollationService);

		_artifactManager = artifactManager;
		_textProvider = textProvider;
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
			final String resultNameFormat = _textProvider.getText(RunFromFileSystemTaskConfigurator.ARTIFACT_NAME_FORMAT_STRING);

			Collection<ResultInfoItem> resultsPathes = TestResultHelper.getTestResults(getResultsFile(), resultsFilter, resultNameFormat, taskContext.getWorkingDirectory(), buildLogger);
			TestResultHelper.publishArtifacts(taskContext, _artifactManager, resultsPathes, buildLogger);
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
