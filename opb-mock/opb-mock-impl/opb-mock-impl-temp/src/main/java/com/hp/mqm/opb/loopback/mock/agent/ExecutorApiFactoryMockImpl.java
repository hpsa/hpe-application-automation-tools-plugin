package com.hp.mqm.opb.loopback.mock.agent;

import com.hp.mqm.opb.ExecutorApiFactory;
import com.hp.mqm.opb.TaskExecutionResult;
import com.hp.mqm.opb.TaskFinishStatus;
import com.hp.mqm.opb.api.TaskOutputData;
import com.hp.mqm.opb.api.TaskProgress;

import java.util.Collections;
import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: ginni
 * Date: 4/15/15
 * mock to Execution API Factory
 */
public class ExecutorApiFactoryMockImpl implements ExecutorApiFactory {
    @Override
    public TaskOutputData createTaskOutputData(byte[] bytes) {
        return new TaskOutputDataMockImpl(bytes);
    }

    @Override
    public TaskProgress createTaskProgress(String description) {
        return new TaskProgressMockImpl(description, 0, Collections.<String, String> emptyMap());
    }

    @Override
    public TaskProgress createTaskProgress(String description, Map<String, String> properties) {
        return new TaskProgressMockImpl(description, 0, properties);
    }

    @Override
    public TaskProgress createTaskProgress(String description, int percentage, Map<String, String> properties) {
        return new TaskProgressMockImpl(description, percentage, properties);
    }
    
    @Override
    public TaskProgress createTaskProgress(String description, int percentage) {
        return createTaskProgress(description, percentage, Collections.<String, String> emptyMap());
    }

    @Override
    public TaskExecutionResult createTaskExecutionResult(TaskFinishStatus status, String result) {
        return new TaskExecutionResultMockImpl(status, result);
    }
}

