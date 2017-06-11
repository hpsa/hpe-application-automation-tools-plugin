package com.hpe.application.automation.tools.sse.sdk.request;

import com.hpe.application.automation.tools.sse.sdk.Client;
import com.hpe.application.automation.tools.sse.sdk.ResourceAccessLevel;
import com.hpe.application.automation.tools.sse.sdk.Response;

/**
 * Created by barush on 29/10/2014.
 */
public abstract class GeneralGetRequest extends GeneralRequest {
    
    protected GeneralGetRequest(Client client) {
        super(client);
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
