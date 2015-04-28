package com.hp.mqm.opb.service.api;


import com.hp.mqm.opb.service.api.callback.OpbDataContainer;
import com.hp.mqm.opb.service.api.entities.OpbTask;

import java.io.InputStream;

/**
 * Abstract factory for creation of OPB API objects.
 * 
 */
public interface OpbObjectFactory {
	/**
	 * Create a domain task.
	 * 
	 * @param description			task description
	 * @param type					task type
	 * @param endpointId			end-point id
	 * @param executorClass			fully qualified name of task executor class
	 * @param configuration			task configuration
	 * @return the created task
	 */
	OpbTask createTask(String description, String type, int endpointId, String executorClass, OpbTaskConfiguration configuration);
	
	/**
	 * Create a platform task or domain task. In case creating and submitting a task as domain task, the task parameters must contains the following parameters
	 * <ul>
	 * <li>{ OpbSharedConsts#ENDPOINT_TYPE_TASK_PARAM}</li>
	 * <li>{ OpbSharedConsts#CREDENTIALS_ID_TASK_PARAM}</li>
	 * <li>{ OpbSharedConsts#ENDPOINT_URL_TASK_PARAM}</li>
	 * </ul> 
	 * 
	 * The following sample code for creating and submitting a domain task for connectivity check
	 * <pre>
	 *      OpbTask task = serviceApi.getObjectFactory().createTask(taskDescription, taskType, agentGuid
	 *      , CONNECTIVITY_CHECK_EXECUTOR_CLASS, CONNECTIVITY_CHECK_CONFIG);
     *      Map<String, String> params = new HashMap<String, String>();
     *      params.put(OpbSharedConsts.CREDENTIALS_ID, "abc");
     *      params.put(OpbSharedConsts.ENDPOINT_TYPE, "alm");
     *      params.put(OpbSharedConsts.ENDPOINT_URL, "http://localhost:8080/qcbin");
     *      task.setParameters(params);
     *      serviceApi.submitTask(task);
	 * </pre> 
	 * 
	 * @param description			task description
	 * @param type					task type
	 * @param agentGuid				agent guid
	 * @param executorClass			fully qualified name of task executor class
	 * @param configuration			task configuration
	 * @return the created task
	 */
	OpbTask createTask(String description, String type, String agentGuid, String executorClass, OpbTaskConfiguration configuration);
    /**
     * Create a task.
     * 
     * @param description           task description
     * @param type                  task type
     * @param endpointId            end-point id
     * @param agentGuid             agent guid
     * @param executorClass         fully qualified name of task executor class
     * @param configuration         task configuration
     * @return the created task
     */
    OpbTask createTask(String description, String type, int endpointId, String agentGuid, String executorClass, OpbTaskConfiguration configuration);
	
	/**
	 * Create a data container.
	 * 
	 * @param size					size of data
	 * @param dataInputStream		data InputStream
	 * @return the created data container
	 */
	OpbDataContainer createDataContainer(long size, InputStream dataInputStream);
	
}
