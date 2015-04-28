package com.hp.mqm.opb.service.api.entities;

/**
 * This interface represents task's result.
 */
public interface OpbTaskResult  {

    /**
     * GGet task id.
     * 
     * @return the task id
     */
    Integer getTaskId();
    /**
     * Set task id.
     * 
     * @param taskId
     */
    void setTaskId(Integer taskId);
    /**
     * Get creation time in milliseconds
     * 
     * @return time in milliseconds
     */
    Double getCreationTime();

    /**
     * Set creation time.
     * 
     * @param time in milliseconds
     */
    void setCreationTime(Double time);

    /**
     * Get agent status.
     * 
     * @return status
     */
    String getStatus();

    /**
     * set agent status.
     * 
     * @param status
     */
    void setStatus(String status);
    /**
     * Get job result content.
     * 
     * @return result content
     */
    String getContent();
    /**
     * Set job result content.
     * 
     * @param content
     */
    void setContent(String content);
}
