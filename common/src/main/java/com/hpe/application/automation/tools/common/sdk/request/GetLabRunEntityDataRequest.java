package com.hpe.application.automation.tools.common.sdk.request;

import com.hpe.application.automation.tools.common.sdk.Client;

/***
 * 
 * @author Effi Bar-She'an
 * @author Dani Schreiber
 * 
 */

public class GetLabRunEntityDataRequest extends GetRequest {
    
    public GetLabRunEntityDataRequest(Client client, String runId) {
        
        super(client, runId);
    }
    
    @Override
    protected String getSuffix() {
        
        return String.format("procedure-runs/%s", _runId);
    }
}
