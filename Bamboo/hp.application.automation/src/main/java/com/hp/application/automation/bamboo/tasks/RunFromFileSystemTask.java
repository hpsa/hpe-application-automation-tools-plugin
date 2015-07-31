package com.hp.application.automation.bamboo.tasks;

import com.atlassian.bamboo.build.BuildRequestResultImpl;
import com.atlassian.bamboo.build.artifact.AbstractArtifactManager;
import com.atlassian.bamboo.build.artifact.ArtifactManager;
import com.atlassian.bamboo.build.test.TestCollationService;
import com.atlassian.bamboo.plan.PlanResultKey;
import com.atlassian.bamboo.task.TaskContext;
import com.atlassian.bamboo.task.TaskException;
import com.atlassian.bamboo.task.TaskResult;
import com.atlassian.bamboo.task.TaskResultBuilder;
import com.atlassian.bamboo.task.TaskType;
import com.atlassian.bamboo.build.logger.BuildLogger;
import com.atlassian.bamboo.configuration.ConfigurationMap;

import java.util.LinkedList;
import java.util.Properties;

import org.jetbrains.annotations.NotNull;

public class RunFromFileSystemTask extends AbstractLauncherTask {

	private final ArtifactManager _artifactManager;

	public RunFromFileSystemTask(@NotNull final TestCollationService testCollationService, @NotNull ArtifactManager artifactManager)
	{
		super(testCollationService);

		_artifactManager = artifactManager;
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
	protected void resultCollated()
	{
		//TODO: Mary try to use this method for publish artifacts.
		// Also add combobox to File System Task UI with options as in Jenkins
		//_artifactManager.publish()
	}
}
