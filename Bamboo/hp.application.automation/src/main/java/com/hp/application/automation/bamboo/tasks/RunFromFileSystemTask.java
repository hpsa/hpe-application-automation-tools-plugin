package com.hp.application.automation.bamboo.tasks;

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

    @java.lang.Override
	protected Properties getTaskProperties(final TaskContext taskContext) throws Exception {
    	Properties result = new Properties();
        
    	final ConfigurationMap map = taskContext.getConfigurationMap();        
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
        
        for(int i=0; i<testNames.length; i++)
        {
        	result.setProperty("Test"+i, testNames[i]);
        }
        
        String timeout = map.get(RunFromFileSystemTaskConfigurator.TIMEOUT);
        
        
        result.setProperty("PerScenarioTimeOut", timeout);
        
    	return result;
	}
}
