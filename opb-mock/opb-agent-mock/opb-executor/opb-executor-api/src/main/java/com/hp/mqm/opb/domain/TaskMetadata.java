package com.hp.mqm.opb.domain;

import com.hp.mqm.opb.api.Endpoint;
import com.hp.mqm.opb.api.TaskId;

import java.util.Properties;

/**
 * This interface describes specific task metadata.
 * 
 * @author avrahame
 */
public interface TaskMetadata {
    
    /**
     * Returns the task identifier.
     * 
     * @return The task identifier.
     */
    public TaskId getId();
    
    /**
     * Returns the task name.
     * 
     * @return The task name.
     */
    public String getName();
    
    /**
     * Returns the task description.
     * 
     * @return The task description.
     */
    public String getDescription();

    /**
     * Returns the task target endpoint.
     * 
     * @return The target endpoint.
     */
    public Endpoint getEndpoint();
    
    /**
     * Returns the task domain parameters.
     * 
     * @return The task domain parameters.
     */
    @Deprecated
    public Properties getDomainParameters();
    
    /**
     * Returns the task parameters.
     * 
     * @return The task parameters.
     */
    public Properties getTaskParameters();
}
