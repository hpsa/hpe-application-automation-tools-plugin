package com.hp.mqm.opb.service.api;

import com.hp.mqm.opb.service.FailedResult;
import com.hp.mqm.opb.service.TaskResult;
import com.hp.mqm.opb.service.api.entities.OpbEndpointType;
import com.hp.mqm.opb.service.api.entities.OpbTask;
import com.hp.mqm.opb.service.api.entities.OpbTaskProgress;
import com.hp.mqm.opb.service.api.scheduler.OpbTaskSchedulerAPI;

import java.util.List;

/**
 * Exposing OPB service api for external usage
 * 
 */
public interface OpbServiceApi {

    public static final String TRUE = "Y";
    public static final String FALSE = "N";

    /**
     * Submit a task.
     * 
     * @param opbTask
     *            the task to be submitted
     */
    int submitTask(OpbTask opbTask);

    /**
     * Update failed task result, completion callback will use this method for
     * recording final result of the task execution.
     * 
     * @param taskId
     *            task id
     * @param result
     *            details errors
     * 
     */
    void updateFailedTaskResult(int taskId, FailedResult result);

    /**
     * Update success task result, completion callback will use this method for
     * recording final result of the task execution.
     * 
     * @param taskId
     *            task id
     * @param resultJson
     *            is formated as json
     * 
     */
    void updateOkTaskResult(int taskId, String resultJson);

    /**
     * Get task result by task id.
     * 
     * @param taskId
     *            task id
     * @return task result
     */
    TaskResult getTaskResult(int taskId);

    /**
     * Get list of OpbEndpointType.
     * 
     * @return a List of OpbEndpointType
     */
    List<OpbEndpointType> getEndpointTypes();

    /**
     * Get OpbObjectFactory.
     * 
     * @return an instance of OpbObjectFactory
     */
    OpbObjectFactory getObjectFactory();

    /**
     * 
     * @param taskId
     * @param status
     * @param description
     * @param percentage
     */
    void updateTaskProgress(int taskId, String status, String description, int percentage);

    /**
     * Get task progresses by task ID
     * 
     * @param taskId
     *            task id
     * @param pageSize
     *            page size
     * @param startIndex
     *            start index
     * @return {@link PageResult<OpbTaskProgress>}
     */
   // PageResult<OpbTaskProgress> getTaskProgresses(int taskId, int pageSize, int startIndex);

    /**
     * Mark an opb task is cancelled.
     * @param taskId
     */
    void cancelTask(int taskId);
    
	/**
	 * Get OPB task scheduler API.
	 * 
	 * @return the OPB task scheduler API.
	 */
	OpbTaskSchedulerAPI getOpbTaskSchedulerAPI();
}
