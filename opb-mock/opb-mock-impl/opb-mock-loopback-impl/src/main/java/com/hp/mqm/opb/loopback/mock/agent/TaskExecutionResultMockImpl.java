package com.hp.mqm.opb.loopback.mock.agent;

import com.hp.mqm.opb.TaskExecutionResult;
import com.hp.mqm.opb.TaskFinishStatus;

/**
 * Created with IntelliJ IDEA.
 * User: ginni
 * Date: 4/15/15
 * mock to task execution result
 */

public class TaskExecutionResultMockImpl implements TaskExecutionResult {
    
    private final TaskFinishStatus status;
    private final String result;
    
    public TaskExecutionResultMockImpl(TaskFinishStatus status, String result) {
        this.status = status;
        this.result = result;
    }
    
    @Override
    public TaskFinishStatus getTaskFinishStatus() {
        return status;
    }
    
    @Override
    public String getTaskResult() {
        return result;
    }
}
