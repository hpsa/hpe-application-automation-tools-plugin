package com.hp.mqm.opb.loopback.mock.service.entities;

import com.hp.mqm.opb.service.api.entities.OpbTaskResult;

/**
 * Created by ginni on 21/04/2015.
 *
 */
public class OpbTaskResultMockImpl implements OpbTaskResult {

    private Integer id;
    private Integer taskId;
    private Double creationTime;
    private String status;
    private String content;

    public OpbTaskResultMockImpl(Integer taskId, Double creationTime, String status, String content) {
        this.taskId = taskId;
        this.creationTime = creationTime;
        this.status = status;
        this.content = content;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
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
    public Double getCreationTime() {
        return creationTime;
    }

    @Override
    public void setCreationTime(Double creationTime) {
        this.creationTime = creationTime;
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
    public String getContent() {
        return content;
    }

    @Override
    public void setContent(String content) {
        this.content = content;
    }
}
