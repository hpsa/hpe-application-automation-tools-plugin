package com.hpe.application.automation.tools.sse.sdk.request;

import com.hpe.application.automation.tools.sse.sdk.Client;

/***
 * 
 * @author Effi Bar-She'an
 * @author Dani Schreiber
 * 
 */

public class GetPCRunEntityDataRequest extends GetRequest {
    
    public GetPCRunEntityDataRequest(Client client, String runId) {
        
        super(client, runId);
    }
    
    @Override
    protected String getSuffix() {
        
        return String.format("runs/%s", _runId);
    }
}
