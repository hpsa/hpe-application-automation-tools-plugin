package com.hp.mqm.opb.loopback.mock.service.entities;

import com.hp.mqm.opb.service.api.entities.OpbTaskProgress;

import java.util.Map;

/**
 * Created by ginni on 21/04/2015.
 *
 */
public class OpbTaskProgressMockImpl implements OpbTaskProgress {
    private Integer id;
    private String guid;
    private Integer taskId;
    private Double updateTime;
    private String description;
    private String status;
    private Integer percentage;
    private Map<String, String> properties;

    public OpbTaskProgressMockImpl(Integer id, String guid, Integer taskId, Double updateTime, String description, String status, Integer percentage, Map<String, String> properties) {
        this.id = id;
        this.guid = guid;
        this.taskId = taskId;
        this.updateTime = updateTime;
        this.description = description;
        this.status = status;
        this.percentage = percentage;
        this.properties = properties;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
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
    public Integer getTaskId() {
        return taskId;
    }

    @Override
    public void setTaskId(Integer taskId) {
        this.taskId = taskId;
    }

    @Override
    public Double getUpdateTime() {
        return updateTime;
    }

    @Override
    public void setUpdateTime(Double updateTime) {
        this.updateTime = updateTime;
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
    public String getStatus() {
        return status;
    }

    @Override
    public void setStatus(String status) {
        this.status = status;
    }

    @Override
    public Integer getPercentage() {
        return percentage;
    }

    @Override
    public void setPercentage(Integer percentage) {
        this.percentage = percentage;
    }

    @Override
    public Map<String, String> getProgressProperties() {
        return properties;
    }

    @Override
    public void setProgressProperties(Map<String, String> properties) {
        this.properties = properties;
    }
}
