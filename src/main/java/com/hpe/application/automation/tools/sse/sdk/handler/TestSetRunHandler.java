package com.hpe.application.automation.tools.sse.sdk.handler;

import com.hpe.application.automation.tools.sse.sdk.Client;

/***
 * 
 * @author Effi Bar-She'an
 * @author Dani Schreiber
 * 
 */
public class TestSetRunHandler extends RunHandler {
    
    public TestSetRunHandler(Client client, String entityId) {
        
        super(client, entityId);
    }
    
    @Override
    protected String getStartSuffix() {
        
        return String.format("test-sets/%s/startruntestset", _entityId);
    }
    
    @Override
    public String getNameSuffix() {
        
        return String.format("test-sets/%s", getEntityId());
    }
}
