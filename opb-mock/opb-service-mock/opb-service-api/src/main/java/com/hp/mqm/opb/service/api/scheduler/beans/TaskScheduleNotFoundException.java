package com.hp.mqm.opb.service.api.scheduler.beans;

/**
 * Indicates that the task schedule was not found.
 * 
 * @author avrahame
 * 
 */
public class TaskScheduleNotFoundException extends RuntimeException {

	private static final long serialVersionUID = 6385858012958331232L;

	public TaskScheduleNotFoundException(String message) {
		super(message);
	}
	
}
