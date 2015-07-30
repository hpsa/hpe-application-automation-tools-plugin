package com.hp.application.automation.tools.common.sdk.request;

import com.hp.application.automation.tools.common.sdk.Client;

/***
 * 
 * @author Effi Bar-She'an
 * @author Dani Schreiber
 * 
 */
public class GetRunEntityNameRequest extends GetRequest {
    
    private final String _nameSuffix;
    
    public GetRunEntityNameRequest(Client client, String suffix, String entityId) {
        
        super(client, entityId);
        _nameSuffix = suffix;
        
    }
    
    @Override
    protected String getSuffix() {
        
        return _nameSuffix;
    }
}
