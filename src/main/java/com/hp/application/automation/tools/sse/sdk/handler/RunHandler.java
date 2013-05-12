package com.hp.application.automation.tools.sse.sdk.handler;

import com.hp.application.automation.tools.model.CdaDetails;
import com.hp.application.automation.tools.sse.sdk.Args;
import com.hp.application.automation.tools.sse.sdk.Client;
import com.hp.application.automation.tools.sse.sdk.Response;
import com.hp.application.automation.tools.sse.sdk.RunResponse;
import com.hp.application.automation.tools.sse.sdk.request.StartRunEntityRequest;
import com.hp.application.automation.tools.sse.sdk.request.StopEntityRequest;

public abstract class RunHandler extends Handler {
    
    protected abstract String getStartSuffix();
    
    public abstract String getNameSuffix();
    
    public RunHandler(Client client, String entityId) {
        
        super(client, entityId);
    }
    
    public Response start(
            String duration,
            String postRunAction,
            String environmentConfigurationId,
            CdaDetails cdaDetails) {
        
        return new StartRunEntityRequest(
                _client,
                getStartSuffix(),
                _entityId,
                duration,
                postRunAction,
                environmentConfigurationId,
                cdaDetails).execute();
    }
    
    public Response stop() {
        
        return new StopEntityRequest(_client, _runId).execute();
    }
    
    public String getReportUrl(Args args) {
        
        return _client.buildWebUIRequest(String.format("lab/index.jsp?processRunId=%s", _runId));
    }
    
    public RunResponse getRunResponse(Response response) {
        
        RunResponse ret = new RunResponse();
        ret.initialize(response);
        
        return ret;
        
    }
}
