package com.hp.application.automation.tools.sse.sdk.request;

import java.util.Map;

import com.hp.application.automation.tools.sse.common.StringUtils;
import com.hp.application.automation.tools.sse.sdk.Client;
import com.hp.application.automation.tools.sse.sdk.Response;

/***
 * 
 * @author Effi Bar-She'an
 * @author Dani Schreiber
 * 
 */

public abstract class Request {
    
    protected final Client _client;
    protected final String _runId;

    public Request(Client client) {

        this(client, StringUtils.EMPTY_STRING);
    }

    protected Request(Client client, String runId) {
        
        _client = client;
        _runId = runId;
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

    protected String getSuffix() {

        return null;
    }
    
    protected Map<String, String> getHeaders() {
        
        return null;
    }
    
    protected String getBody() {
        
        return null;
    }
    
    protected String getUrl() {
        
        return _client.buildRestRequest(getSuffix());
    }
}
