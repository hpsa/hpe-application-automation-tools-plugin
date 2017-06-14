package com.hpe.application.automation.tools.sse.sdk.request;

import com.hpe.application.automation.tools.sse.sdk.Client;

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
        return "procedure-testset-instance-runs";
    }
    
    @Override
    protected String getQueryString() {
        
        return String.format("query={procedure-run[%s]}&page-size=2000", _runId);
    }
    
}
