package com.hpe.application.automation.tools.sse.sdk.request;

import com.hpe.application.automation.tools.sse.sdk.Client;

/***
 * 
 * @author Effi Bar-She'an
 * @author Dani Schreiber
 * 
 */

public abstract class GetRequest extends GeneralGetRequest {
    
    protected final String _runId;
    
    protected GetRequest(Client client, String runId) {
        
        super(client);
        _runId = runId;
    }
    
}
