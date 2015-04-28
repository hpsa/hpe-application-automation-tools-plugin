package com.hp.mqm.opb.loopback.mock.service.entities;

import com.hp.mqm.opb.service.api.entities.OpbEndpoint;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by ginni on 20/04/2015.
 *
 */
public class OpbEndpointMockImpl implements OpbEndpoint {

    private Integer id;
    private String name;
    private String description;
    private String status;
    private Integer agentId;
    private Integer revision;
    private String endpointType;
    private String credentialsId;
    private String credentialsDesc;
    private Boolean isDefault;
    private String uniquenessIdent;
    private Map<String, String> parameters;

    public OpbEndpointMockImpl(Integer id, Integer agentId) {
        this.id = id;
        this.name = "mock endpoint";
        this.description = "description";
        this.status = "RUNNING";
        this.agentId = agentId;
        this.revision = 13;
        this.endpointType = "test-domain";
        this.credentialsId = "credId";
        this.credentialsDesc = "credentialsDesc";
        this.isDefault = false;
        this.uniquenessIdent = "";
        this.parameters = new HashMap<>();
    }

    @Override
    public int getId() {
        return id;
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
        return endpointType;
    }

    @Override
    public void setType(String type) {
        this.endpointType = type;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void setName(String name) {
        this.name = name;
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
    public Integer getAgentId() {
        return agentId;
    }

    @Override
    public void setAgentId(Integer agentId) {
        this.agentId = agentId;
    }

    @Override
    public String getCredentialsId() {
        return credentialsId;
    }

    @Override
    public void setCredentialsId(String credentialsId) {
        this.credentialsId = credentialsId;
    }

    @Override
    public String getCredentialsDescription() {
        return credentialsDesc;
    }

    @Override
    public void setCredentialsDescription(String credDesc) {
        this.credentialsDesc = credDesc;
    }

    @Override
    public Boolean getIsDefault() {
        return isDefault;
    }

    @Override
    public void setIsDefault(Boolean isDefault) {
        this.isDefault = isDefault;
    }

    @Override
    public String getUniquenessIdent() {
        return uniquenessIdent;
    }

    @Override
    public void setUniquenessIdent(String uniqIdent) {
        this.uniquenessIdent = uniqIdent;
    }

    @Override
    public Map<String, String> getParameters() {
        return parameters;
    }

    @Override
    public void setParameters(Map<String, String> params) {
        this.parameters = params;
    }

    @Override
    public Integer getRevision() {
        return revision;
    }

    @Override
    public void setRevision(Integer revision) {
        this.revision = revision;
    }
}
