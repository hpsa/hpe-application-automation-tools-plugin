package com.hp.application.automation.tools.common.sdk.request;

import com.hp.application.automation.tools.common.sdk.Client;

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
