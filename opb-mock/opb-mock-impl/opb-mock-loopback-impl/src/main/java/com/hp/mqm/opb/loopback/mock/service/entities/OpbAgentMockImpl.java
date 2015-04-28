package com.hp.mqm.opb.loopback.mock.service.entities;

import com.hp.mqm.opb.service.api.entities.OpbAgent;

/**
 * Created by ginni on 20/04/2015.
 *
 */
public class OpbAgentMockImpl implements OpbAgent {
    private Integer id;
    private String guid;
    private String description;
    private String name;
    private String version;
    private String isVersionSupported;
    private String status;
    private Double lastSeen;

    public OpbAgentMockImpl(Integer id, String guid) {
        setId(id);
        setGuid(guid);
    }

    public void setId(Integer id) {
        this.id = id;
    }

    @Override
    public String getGuid() {
        return guid;
    }

    @Override
    public Integer getId() {
        return id;
    }

    @Override
    public void setGuid(String guid) {
        this.guid = guid;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void setName(String agentName) {
        this.name = agentName;
    }

    @Override
    public String getVersion() {
        return version;
    }

    @Override
    public void setVersion(String version) {
        this.version = version;
    }

    @Override
    public String getIsVersionSupported() {
        return isVersionSupported;
    }

    @Override
    public void setIsVersionSupported(String isVersionSupported) {
        this.isVersionSupported = isVersionSupported;
    }

    @Override
    public String getDescription() {
        return description;
    }

    @Override
    public void setDescription(String description) {
        this.description = description ;
    }

    @Override
    public Double getLastSeen() {
        return lastSeen;
    }

    @Override
    public void setLastSeen(Double lastSeen) {
        this.lastSeen = lastSeen;
    }

    @Override
    public String getStatus() {
        return status;
    }

    @Override
    public void setStatus(String status) {
        this.status = status;
    }
}
