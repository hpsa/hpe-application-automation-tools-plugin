package com.hp.mqm.opb.service.api.entities;

import java.util.Map;

/**
 * Created with IntelliJ IDEA. User: Phong Date: 1/16/14 Time: 1:52 PM To change
 * this template use File | Settings | File Templates.
 */
public interface OpbTaskProgress {

       /**
     * Get task guid.
     * 
     * @return guid
     */
    String getGuid();

    /**
     * Set task guid
     * 
     * @param guid
     */
    void setGuid(String guid);

    /**
     * Get task id
     * 
     * @return task id
     */
    Integer getTaskId();

    /**
     * Set task id
     * 
     * @param taskId
     */
    void setTaskId(Integer taskId);

    /**
     * Get task status.
     * 
     * @return task status
     */
    String getStatus();

    /**
     * Set task status
     * 
     * @param status
     */
    void setStatus(String status);

    /**
     * Get description.
     * 
     * @return description
     */
    String getDescription();

    /**
     * Set description.
     * 
     * @param description
     */
    void setDescription(String description);

    /**
     * Get updated time.
     * 
     * @return updated time in milliseconds
     */
    Double getUpdateTime();

    /**
     * Set updated time.
     * 
     * @param updateTime
     */
    void setUpdateTime(Double updateTime);

    /**
     * Get percentage of progress
     * 
     * @return percentage
     */
    Integer getPercentage();

    /**
     * Set percentage of progress.
     * 
     * @param percentage
     */
    void setPercentage(Integer percentage);

    /**
     * Get progress properties
     * 
     * @return properties
     */
    Map<String, String> getProgressProperties();

    /**
     * Set progress properties.
     * 
     * @param properties
     */
    void setProgressProperties(Map<String, String> properties);
}
