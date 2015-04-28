package com.hp.mqm.opb.service.api;

import com.hp.mqm.opb.service.TaskPriority;
import com.hp.mqm.opb.service.api.callback.IncomingDataCallback;
import com.hp.mqm.opb.service.api.callback.OutgoingDataCallback;
import com.hp.mqm.opb.service.api.callback.TaskResponseCallback;

/**
 * Keep task configuration. 
 * 
 */
public class OpbTaskConfiguration {

	private TaskPriority taskPriority;
	private int timeoutSeconds;
	private Class<? extends IncomingDataCallback> incomingDataCallback;
	private Class<? extends OutgoingDataCallback> outgoingDataCallback;
	private Class<? extends TaskResponseCallback> taskResponseCallback;
	
	/**
	 * Create a new instance of task configuration with default task priority OpbTaskPriority.SYNC_TASK.
	 * @param incomingDataCallback implementation class of IncomingDataCallback
	 * @param outgoingDataCallback implementation class of OutgoingDataCallback
	 * @param taskResponseCallback implementation class of TaskResponseCallback
	 */
	public OpbTaskConfiguration(Class<? extends IncomingDataCallback> incomingDataCallback,
								Class<? extends OutgoingDataCallback> outgoingDataCallback,
								Class<? extends TaskResponseCallback> taskResponseCallback) {
		
		this(OpbTaskPriority.SYNC_TASK, incomingDataCallback, outgoingDataCallback, taskResponseCallback);
	}
	
	/**
	 * Create a new instance of task configuration, use predefined combination of task priority and timeout.
	 * @param opbTaskPriority predefined task priority and timeout, user one of OpbTaskPriority.SYNC_TASK or OpbTaskPriority.AD_HOC_TASK
	 * @param incomingDataCallback implementation class of IncomingDataCallback
	 * @param outgoingDataCallback implementation class of OutgoingDataCallback
	 * @param taskResponseCallback implementation class of TaskResponseCallback
	 */
	public OpbTaskConfiguration(OpbTaskPriority opbTaskPriority, Class<? extends IncomingDataCallback> incomingDataCallback,
								Class<? extends OutgoingDataCallback> outgoingDataCallback,
								Class<? extends TaskResponseCallback> taskResponseCallback) {
		this(opbTaskPriority.getPriority(), opbTaskPriority.getDefaultTimeout(), incomingDataCallback, outgoingDataCallback, taskResponseCallback);
	}
	
	/**
	 * Create a new instance of task configuration.
	 * @param taskPriority task priority
	 * @param timeoutSeconds timeout in seconds
	 * @param incomingDataCallback implementation class of IncomingDataCallback
	 * @param outgoingDataCallback implementation class of OutgoingDataCallback
	 * @param taskResponseCallback implementation class of TaskResponseCallback
	 */
	public OpbTaskConfiguration(TaskPriority taskPriority, int timeoutSeconds,
								Class<? extends IncomingDataCallback> incomingDataCallback,
								Class<? extends OutgoingDataCallback> outgoingDataCallback,
								Class<? extends TaskResponseCallback> taskResponseCallback) {
		super();
		this.taskPriority = taskPriority;
		this.timeoutSeconds = timeoutSeconds;
		this.incomingDataCallback = incomingDataCallback;
		this.outgoingDataCallback = outgoingDataCallback;
		this.taskResponseCallback = taskResponseCallback;
	}



	/**
     * Priority of task.
     *
     * @return the task priority.
     */
	public TaskPriority getTaskPriority() {
		return taskPriority;
	}
	
	/**
     * @return the timeout for the task. If timeout is exceeded and the task is still not complete -
     * will return an error to the result callback (in case it was specified).
     */
	public int getTimeoutSeconds() {
       	return 1;
	}
	
	/**
     * Get IncomingDataCallback class. 
     * @return a sub class of IncomingDataCallback 
     */
	public Class<? extends IncomingDataCallback> getIncomingDataCallback() {
		return incomingDataCallback;
	}
	
	/**
     * Get OutgoingDataCallback class name. 
     * @return a sub class of OutgoingDataCallback 
     */
	public Class<? extends OutgoingDataCallback> getOutgoingDataCallback() {
		return outgoingDataCallback;
	}
	
	/**
     * Get TaskResponseCallback class name. 
     * @return a sub class of TaskResponseCallback 
     */
	public Class<? extends TaskResponseCallback> getTaskResponseCallback() {
		return taskResponseCallback;
	}
}

