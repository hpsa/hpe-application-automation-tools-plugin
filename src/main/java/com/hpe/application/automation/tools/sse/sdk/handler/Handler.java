package com.hpe.application.automation.tools.sse.sdk.handler;

import com.hpe.application.automation.tools.sse.common.StringUtils;
import com.hpe.application.automation.tools.sse.sdk.Client;

public abstract class Handler {
    
    protected final Client _client;
    protected final String _entityId;
    protected String _runId = StringUtils.EMPTY_STRING;
    protected String _timeslotId = StringUtils.EMPTY_STRING;
    
    public Handler(Client client, String entityId) {
        
        _client = client;
        _entityId = entityId;
    }
    
    public Handler(Client client, String entityId, String runId) {
        
        this(client, entityId);
        _runId = runId;
    }
    
    public String getRunId() {
        
        return _runId;
    }
    
    public String getEntityId() {
        
        return _entityId;
    }
    
    public void setRunId(String runId) {
        _runId = runId;
    }
    
    public String getTimeslotId() {
        
        return _timeslotId;
    }
    
    public void setTimeslotId(String timeslotId) {
        
        _timeslotId = timeslotId;
    }
}
