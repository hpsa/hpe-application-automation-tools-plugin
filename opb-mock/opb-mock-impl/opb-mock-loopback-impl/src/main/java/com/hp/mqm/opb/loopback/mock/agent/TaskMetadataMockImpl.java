package com.hp.mqm.opb.loopback.mock.agent;

import com.hp.mqm.opb.api.Endpoint;
import com.hp.mqm.opb.api.TaskId;
import com.hp.mqm.opb.domain.TaskMetadata;

import java.util.Properties;

/**
 * Created with IntelliJ IDEA.
 * User: ginni
 * Date: 4/15/15
 * mock to task metadata
 */
public class TaskMetadataMockImpl implements TaskMetadata {
    
    private TaskId id;
    private String name;
    private String description;
    private Endpoint endpoint;
    private Properties parameters;
    
    public TaskMetadataMockImpl(
            TaskId id,
            String name,
            String description,
            Endpoint endpoint,
            Properties parameters) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.endpoint = endpoint;
        this.parameters = parameters;
    }
    
    @Override
    public TaskId getId() {
        return id;
    }
    
    @Override
    public String getName() {
        return name;
    }
    
    @Override
    public String getDescription() {
        return description;
    }
    
    @Override
    public Endpoint getEndpoint() {
        return endpoint;
    }
    
    @Override
    public Properties getDomainParameters() {
        return getTaskParameters();
    }
    
    @Override
    public Properties getTaskParameters() {
        return parameters;
    }
}
