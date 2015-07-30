package com.hp.application.automation.tools.common.sdk.request;

import com.hp.application.automation.tools.common.sdk.Client;

/***
 * 
 * @author Effi Bar-She'an
 * @author Dani Schreiber
 * 
 */
public class GetPCRunEntityTestSetRunsRequest extends GetRequest {
    
    public GetPCRunEntityTestSetRunsRequest(Client client, String runId) {
        
        super(client, runId);
    }
    
    @Override
    protected String getSuffix() {
        
        return String.format("runs/%s", _runId);
    }
}
