package com.hpe.application.automation.tools.common.sdk.request;

import com.hpe.application.automation.tools.common.sdk.Client;

/***
 * 
 * @author Effi Bar-She'an
 * @author Dani Schreiber
 * 
 */
public abstract class PostRequest extends GeneralPostRequest {
    
    protected final String _runId;
    
    protected PostRequest(Client client, String runId) {
        
        super(client);
        _runId = runId;
    }
    
}
