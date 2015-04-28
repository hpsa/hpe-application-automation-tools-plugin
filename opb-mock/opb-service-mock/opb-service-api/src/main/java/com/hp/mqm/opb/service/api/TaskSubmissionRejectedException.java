package com.hp.mqm.opb.service.api;

/**
 * When thrown by the {@link OpbTaskEventHandler} beforeSubmit method the task
 * submission is rejected.
 * 
 * @author avrahame
 * 
 */
public class TaskSubmissionRejectedException extends RuntimeException {
	private static final long serialVersionUID = -5901484372598081204L;

	public TaskSubmissionRejectedException(String message) {
		super(message);
	}


}
