package com.hp.application.automation.tools.common.sdk.request;

import com.hp.application.automation.tools.common.sdk.Client;
import com.hp.application.automation.tools.common.sdk.ResourceAccessLevel;
import com.hp.application.automation.tools.common.sdk.Response;

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
        
        return client.httpGet(
                getUrl(),
                getQueryString(),
                getHeaders(),
                ResourceAccessLevel.PROTECTED);
    }
}
