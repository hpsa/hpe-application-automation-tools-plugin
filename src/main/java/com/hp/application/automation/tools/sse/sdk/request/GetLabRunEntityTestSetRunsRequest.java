package com.hp.application.automation.tools.sse.sdk.request;

import com.hp.application.automation.tools.sse.sdk.Client;

/***
 * 
 * @author Effi Bar-She'an
 * @author Dani Schreiber
 * 
 */
public class GetLabRunEntityTestSetRunsRequest extends GetRequest {
    
    public GetLabRunEntityTestSetRunsRequest(Client client, String runId) {
        
        super(client, runId);
    }
    
    @Override
    protected String getSuffix() {
        
        return String.format("procedure-testset-instance-runs?query={procedure-run[%s]}", _runId);
    }
}
