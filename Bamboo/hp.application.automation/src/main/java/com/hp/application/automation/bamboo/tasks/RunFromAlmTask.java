package com.hp.application.automation.bamboo.tasks;

import java.util.Properties;

import com.atlassian.bamboo.task.TaskContext;
import com.atlassian.bamboo.task.TaskException;
import com.atlassian.bamboo.task.TaskResult;
import com.atlassian.bamboo.task.TaskResultBuilder;
import com.atlassian.bamboo.task.TaskType;
import org.jetbrains.annotations.NotNull;

public class RunFromAlmTask extends AbstractLauncherTask {

    @java.lang.Override
	protected Properties getTaskProperties(final TaskContext taskContext) throws Exception {
    	return null;
	}
    
}
