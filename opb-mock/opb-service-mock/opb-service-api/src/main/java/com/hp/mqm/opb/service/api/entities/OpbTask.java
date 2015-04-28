package com.hp.mqm.opb.service.api.entities;


import com.hp.mqm.opb.service.api.OpbTaskEventHandler;

import java.util.Map;

/**
 * This interface represents task in OPB.
 */
public interface OpbTask  {

    int getId();
    /**
     * Get task guid
     * 
     * @return guid
     */
    String getGuid();

    /**
     * Set task guid.
     * 
     * @param guid
     */
    void setGuid(String guid);

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
     * Get type.
     * 
     * @return type
     */
    String getType();

    /**
     * Set type.
     * 
     * @param type
     */
    void setType(String type);

    /**
     * Get endpoint id.
     * 
     * @return endpoint id
     */
    Integer getEndpointId();

    /**
     * Set endpoint id
     * 
     * @param endpointId
     */
    void setEndpointId(Integer endpointId);

    /**
     * Get endoint runtime id
     *
     * @return endpoint runtime id
     */
    Integer getEndpointRuntimeId();

    /**
     * set endpoint runtime id
     * @param endpointRuntimeId
     */
    void setEndpointRuntimeId(Integer endpointRuntimeId);

    /**
     * Get executor class name.
     * 
     * @return class name
     */
    String getExecutorClass();

    /**
     * Set executor class name.
     * 
     * @param executorClass
     */
    void setExecutorClass(String executorClass);

    /**
     * Get submitted time.
     * 
     * @return submitted time
     */
    Double getSubmitTime();

    /**
     * Set submitted time.
     * 
     * @param submitTime
     */
    void setSubmitTime(Double submitTime);

    /**
     * Get is handled value.
     * 
     * @return boolean
     */
    Boolean getIsHandled();

    /**
     * Set is handled value.
     * 
     * @param isHandled
     */
    void setIsHandled(Boolean isHandled);

    /**
     * Get incoming backend point.
     * 
     * @return class name
     */
    String getIncomingBackendPoint();

    /**
     * Set incoming backend point
     * 
     * @param point
     *            class name
     */
    void setIncomingBackendPoint(String point);

    /**
     * Get outgoing backend point.
     * 
     * @return class name
     */
    String getOutgoingBackendPoint();

    /**
     * Set outgoing backend point.
     * 
     * @param point
     *            class name
     */
    void setOutgoingBackendPoint(String point);

    /**
     * Get response backend point.
     * 
     * @return class name
     */
    String getResponseBackendPoint();

    /**
     * Set response backend point.
     * 
     * @param point
     *            class name
     */
    void setResponseBackendPoint(String point);
    

    /**
     * Get priority
     * 
     * @return priority name
     */
    String getPriority();

    /**
     * Set priority name.
     * 
     * @param priority
     */
    void setPriority(String priority);

    /**
     * Get timeout seconds
     * 
     * @return timeout seconds
     */
    Integer getTimeoutSeconds();

    /**
     * Set timeout seconds
     * 
     * @param timeoutSeconds
     */
    void setTimeoutSeconds(Integer timeoutSeconds);

    Double getLastActivityTimestamp();

    void setLastActivityTimestamp(Double lastActivityTimestamp);

    /**
     * Get parameters.
     * 
     * @return parameters
     */
    Map<String, String> getParameters();

    /**
     * Set parameters.
     * 
     * @param parameters
     */
    void setParameters(Map<String, String> parameters);

    /**
     * Get agent guid
     * 
     * @return agent guid
     */
    String getAgentGuid();

    /**
     * Set agent guid.
     * 
     * @param agentGuid
     */
    void setAgentGuid(String agentGuid);
    
    /**
     * Get persistent result
     * @return boolean
     */
    Boolean isPersistentResult();
    /**
     * Set persistent result
     *  
     * @param persistentResult
     */
    void setPersistentResult(Boolean persistentResult);

    /**
     * Get is cancelled value.
     *
     * @return boolean
     */
    Boolean isCancelled();

    /**
     * Set is cancelled value.
     *
     * @param isCancelled
     */
    void setIsCancelled(Boolean isCancelled);
    
    /**
     * Get the task events handler.
     * 
     * @return class name
     */
    OpbTaskEventHandler getTaskEventHandler();

    /**
     * Set task events handler e.g. before task is submitted and after the task is submitted. The handler will be called by OPB platform for scheduled tasks.
     * 
     * @param handler
     *            Task event handler.
     */
    void setTaskEventHandler(OpbTaskEventHandler handler);

}
