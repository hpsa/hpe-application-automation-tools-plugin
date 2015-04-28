package com.hp.mqm.opb;

/**
 * Task execution result.
 *
 * User: ginni
 * Date: 4/15/15
 */
public interface TaskExecutionResult {
    
    /**
     * Returns task finish status.
     * 
     * @return task finish status.
     */
    public TaskFinishStatus getTaskFinishStatus();
    
    /**
     * Returns task result string.
     * 
     * @return task result string.
     */
    public String getTaskResult();
}
