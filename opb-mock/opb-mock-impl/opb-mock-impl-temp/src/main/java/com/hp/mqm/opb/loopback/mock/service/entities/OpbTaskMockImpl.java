package com.hp.mqm.opb.loopback.mock.service.entities;

import com.hp.mqm.opb.service.api.OpbTaskEventHandler;
import com.hp.mqm.opb.service.api.entities.OpbTask;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by ginni on 20/04/2015.
 *
 */
public class OpbTaskMockImpl implements OpbTask {

    private int id;
    private String guid;
    private String description;
    private String type;
    private Integer endpointId;
    private Integer endpointRuntimeId;
    private String executorClass;
    private Double submitTime;
    private Boolean isHandled;
    private String outgoingBackendPoint;
    private String response;
    private String priority;
    private Integer timeoutSeconds;
    private Double lastActivityTimestamp;
    private String agentGuid;
    private Map<String, String> parameters;
    private Boolean isCancelled;
    private String incomingBackendPoint;
    private Boolean isPersistentResult;
    private OpbTaskEventHandler opbTaskEventHandler;

    public OpbTaskMockImpl(Integer id, Integer endpointId, Integer endpointRuntimeId, String agentGuid, String executorClass){
        this.id = id;
        this.guid = id.toString();
        this.description = "desc";
        this.type = "sync";
        this.endpointId = endpointId;
        this.endpointRuntimeId = endpointRuntimeId;
        this.executorClass = executorClass;
        this.submitTime = 0.0;
        this.isHandled = false;
        this.priority = "ADHOC";
        this.timeoutSeconds = 10;
        this.lastActivityTimestamp = submitTime;
        this.agentGuid = agentGuid;
        this.parameters = new HashMap<>();
        this.isCancelled = false;
        this.isPersistentResult = false;
    }


    @Override
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    @Override
    public String getGuid() {
        return guid;
    }

    @Override
    public void setGuid(String guid) {
        this.guid = guid;
    }

    @Override
    public String getDescription() {
        return description;
    }

    @Override
    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public String getType() {
        return type;
    }

    @Override
    public void setType(String type) {
        this.type = type;
    }

    @Override
    public Integer getEndpointId() {
        return endpointId;
    }

    @Override
    public void setEndpointId(Integer endpointId) {
        this.endpointId = endpointId;
    }

    @Override
    public Integer getEndpointRuntimeId() {
        return endpointRuntimeId;
    }

    @Override
    public void setEndpointRuntimeId(Integer endpointRuntimeId) {
        this.endpointRuntimeId = endpointRuntimeId;
    }

    @Override
    public String getExecutorClass() {
        return executorClass;
    }

    @Override
    public void setExecutorClass(String executorClass) {
        this.executorClass = executorClass;
    }

    @Override
    public Double getSubmitTime() {
        return submitTime;
    }

    @Override
    public void setSubmitTime(Double submitTime) {
        this.submitTime = submitTime;
    }
    @Override
    public String getIncomingBackendPoint() {
        return incomingBackendPoint;
    }

    @Override
    public void setIncomingBackendPoint(String point) {
        this.incomingBackendPoint = point;
    }

    @Override
    public String getOutgoingBackendPoint() {
        return outgoingBackendPoint;
    }

    @Override
    public void setOutgoingBackendPoint(String point) {
        this.outgoingBackendPoint = point;
    }

    @Override
    public String getResponseBackendPoint() {
        return response;
    }

    @Override
    public void setResponseBackendPoint(String point) {
        this.response = point;
    }

    @Override
    public Boolean getIsHandled() {
        return isHandled;
    }

    public void setIsHandled(Boolean isHandled) {
        this.isHandled = isHandled;
    }

    @Override
    public String getPriority() {
        return priority;
    }

    @Override
    public void setPriority(String priority) {
        this.priority = priority;
    }

    @Override
    public Integer getTimeoutSeconds() {
        return timeoutSeconds;
    }

    @Override
    public void setTimeoutSeconds(Integer timeoutSeconds) {
        this.timeoutSeconds = timeoutSeconds;
    }

    @Override
    public Double getLastActivityTimestamp() {
        return lastActivityTimestamp;
    }

    @Override
    public void setLastActivityTimestamp(Double lastActivityTimestamp) {
        this.lastActivityTimestamp = lastActivityTimestamp;
    }

    @Override
    public String getAgentGuid() {
        return agentGuid;
    }

    @Override
    public void setAgentGuid(String agentGuid) {
        this.agentGuid = agentGuid;
    }

    @Override
    public Boolean isPersistentResult() {
        return isPersistentResult;
    }

    @Override
    public void setPersistentResult(Boolean persistentResult) {
        this.isPersistentResult = persistentResult;
    }

    @Override
    public Boolean isCancelled() {
        return isCancelled;
    }

    @Override
    public void setIsCancelled(Boolean isCancelled) {
        this.isCancelled = isCancelled;
    }

    @Override
    public OpbTaskEventHandler getTaskEventHandler() {
        return opbTaskEventHandler;
    }

    @Override
    public void setTaskEventHandler(OpbTaskEventHandler handler) {
        this.opbTaskEventHandler = handler;
    }

    @Override
    public Map<String, String> getParameters() {
        return parameters;
    }

    @Override
    public void setParameters(Map<String, String> parameters) {
        this.parameters = parameters;
    }
}
