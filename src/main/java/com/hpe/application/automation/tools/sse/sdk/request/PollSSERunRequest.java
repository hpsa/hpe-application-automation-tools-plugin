package com.hpe.application.automation.tools.sse.sdk.request;

import com.hpe.application.automation.tools.sse.sdk.Client;

/***
 * 
 * @author Effi Bar-She'an
 * @author Dani Schreiber
 * 
 */

public class PollSSERunRequest extends GetRequest {
    
    private final String _runId;
    
    public PollSSERunRequest(Client client, String runId) {
        
        super(client, runId);
        _runId = runId;
    }
    
    @Override
    protected String getSuffix() {
        
        return String.format("procedure-runs/%s", _runId);
    }
}
