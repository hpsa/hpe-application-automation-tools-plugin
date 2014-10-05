package com.hp.application.automation.tools.sse.sdk.request;

import com.hp.application.automation.tools.sse.sdk.Client;
import com.hp.application.automation.tools.sse.sdk.ResourceAccessLevel;
import com.hp.application.automation.tools.sse.sdk.Response;

/***
 * 
 * @author Effi Bar-She'an
 * @author Dani Schreiber
 * 
 */

public abstract class GetRequest extends Request {
    
    protected GetRequest(Client client, String runId) {
        
        super(client, runId);
    }
    
    protected String getQueryString() {
        
        return null;
    }
    
    @Override
    public Response perform() {
        
        return _client.httpGet(
                getUrl(),
                getQueryString(),
                getHeaders(),
                ResourceAccessLevel.PROTECTED);
    }
    
}
