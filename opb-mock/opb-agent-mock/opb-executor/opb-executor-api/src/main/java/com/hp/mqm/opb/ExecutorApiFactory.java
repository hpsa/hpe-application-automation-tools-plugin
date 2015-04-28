package com.hp.mqm.opb;

import com.hp.mqm.opb.api.TaskOutputData;
import com.hp.mqm.opb.api.TaskProgress;

import java.util.Map;

/**
 * Platform Executor API Factory.
 *
 * User: ginni
 * Date: 4/15/15
 */
public interface ExecutorApiFactory {
    
    /**
     * Create task output data instance.
     * 
     * @param bytes
     *            The data as byte array.
     * @return TaskOutputData instance with the content given as parameter.
     */
    TaskOutputData createTaskOutputData(byte[] bytes);

    /**
     * Create task progress instance. Setting percentage to the task's current percentage
     *
     * @param description
     *            String describing the progress.
     * @return TaskProgress instance.
     */
    TaskProgress createTaskProgress(
            String description);

    /**
     * Create task progress instance. Setting percentage to the task's current percentage
     *
     * @param description
     *            String describing the progress.
     * @param properties
     *            Properties related to the task progress.
     * @return TaskProgress instance.
     */
    TaskProgress createTaskProgress(
            String description,
            Map<String, String> properties);
    
    /**
     * Create task progress instance.
     * 
     * @param description
     *            String describing the progress.
     * @param percentage
     *            The task progress in percentages as in between 0-100.
     * @param properties
     *            Properties related to the task progress.
     * @return TaskProgress instance.
     */
    TaskProgress createTaskProgress(
            String description,
            int percentage,
            Map<String, String> properties);
    
    /**
     * Create task progress instance.
     * 
     * @param description
     *            String describing the progress.
     * @param percentage
     *            The task progress in percentages as in between 0-100.
     * @return TaskProgress instance.
     */
    TaskProgress createTaskProgress(
            String description,
            int percentage);
    
    /**
     * Create task execution result instance.
     * 
     * @param status
     *            The task finish status.
     * @param result
     *            Result string to return to the relevant callback.
     * @return TaskExecutionResult instance.
     */
    TaskExecutionResult createTaskExecutionResult(TaskFinishStatus status, String result);
}
