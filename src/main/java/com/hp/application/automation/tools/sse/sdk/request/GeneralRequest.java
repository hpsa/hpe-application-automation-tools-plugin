package com.hp.application.automation.tools.sse.sdk.request;

import java.util.Map;

import com.hp.application.automation.tools.sse.sdk.Client;
import com.hp.application.automation.tools.sse.sdk.Response;

/**
 * Created by barush on 29/10/2014.
 */
public abstract class GeneralRequest {
    
    protected final Client client;
    
    protected GeneralRequest(Client client) {
        this.client = client;
    }
    
    public final Response execute() {
        
        Response ret = new Response();
        try {
            ret = perform();
        } catch (Throwable cause) {
            ret.setFailure(cause);
        }
        
        return ret;
    }
    
    protected abstract Response perform();
    
    protected abstract String getSuffix();
    
    protected Map<String, String> getHeaders() {
        
        return null;
    }
    
    protected String getBody() {
        
        return null;
    }
    
    protected String getUrl() {
        
        return client.buildRestRequest(getSuffix());
    }
    
}
