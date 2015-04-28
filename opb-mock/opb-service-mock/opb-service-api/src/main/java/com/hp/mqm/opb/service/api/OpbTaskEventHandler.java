package com.hp.mqm.opb.service.api;

import com.hp.mqm.opb.service.api.entities.OpbTask;

/**
 * Handles task life cycle events. Implement this interface in order to perform
 * additional logic in the hooks provided by this handler. This handler is
 * needed when the task is created and submitted by OPB platform task scheduler.
 * <p>
 * To apply the handler use:
 * <p>
 * <code> <p>OpbTask task= createTask();
 * </p>
 * <p>
 * task.setTaskEventHandler(handler).
 * </p>
 * </code></p> </p>
 * 
 * @author avrahame
 * 
 */
public interface OpbTaskEventHandler {

	/**
	 * Will be called before the task is submitted.
	 * 
	 * @param task
	 *            The task that will be submitted.
	 * @throws TaskSubmissionRejectedException
	 *             In order to reject task submission.
	 */
	void beforeSubmit(OpbTask task) throws TaskSubmissionRejectedException;

	/**
	 * Will be called after the task was submitted.
	 * <p>Note: In case exception is thrown from this method the task submission will fail.</p> 
	 *
	 */
	void afterSubmit(OpbTask task);

	
	/**
	 * Will be called on submit failure.
	 * 
	 * @param task
	 *            The task that failed to be submitted.
	 * @param e
	 *            The exception occurred while submitting the task.
	 */
	void submitFailed(OpbTask task, Exception e);
}
