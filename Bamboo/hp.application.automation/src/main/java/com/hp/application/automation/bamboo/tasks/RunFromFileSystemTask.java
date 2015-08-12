package com.hp.application.automation.bamboo.tasks;

import com.atlassian.bamboo.build.BuildLoggerManager;
import com.atlassian.bamboo.build.artifact.ArtifactManager;
import com.atlassian.bamboo.build.test.TestCollationService;
import com.atlassian.bamboo.task.*;
import com.atlassian.bamboo.build.logger.BuildLogger;
import com.atlassian.bamboo.configuration.ConfigurationMap;

import java.io.File;
import java.util.*;

import com.atlassian.bamboo.v2.build.BuildContext;
import com.hp.application.automation.tools.common.sdk.Logger;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class RunFromFileSystemTask extends AbstractLauncherTask {

	private final ArtifactManager _artifactManager;
	private final BuildLoggerManager _buildLoggerManager;

	public RunFromFileSystemTask(@NotNull final TestCollationService testCollationService, @NotNull ArtifactManager artifactManager, @NotNull BuildLoggerManager buildLoggerManager)
	{
		super(testCollationService);

		_artifactManager = artifactManager;
		_buildLoggerManager = buildLoggerManager;
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
		final BuildLogger buildLogger = taskContext.getBuildLogger();
		Logger logger = new Logger() {
			@Override
			public void log(String message) {
				buildLogger.addBuildLogEntry(message);
			}
		};

		Collection<String> resultsPathes = TestResultHelper.getTestResultsPathes(getResultsFile(), TestResultHelper.ResultTypeFilter.All, logger);
		TestResultHelper.publishArtifacts(taskContext, _artifactManager, resultsPathes, buildLogger);
	}
}
