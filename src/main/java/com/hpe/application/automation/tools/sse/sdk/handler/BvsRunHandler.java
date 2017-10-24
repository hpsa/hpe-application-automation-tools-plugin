package com.hpe.application.automation.tools.sse.sdk.handler;

import com.hpe.application.automation.tools.sse.sdk.Client;

/***
 * 
 * @author Effi Bar-She'an
 * @author Dani Schreiber
 * 
 */
public class BvsRunHandler extends RunHandler {
    
    public BvsRunHandler(Client client, String entityId) {
        
        super(client, entityId);
    }
    
    @Override
    protected String getStartSuffix() {
        
        return String.format("procedures/%s/startrunprocedure", _entityId);
    }
    
    @Override
    public String getNameSuffix() {
        
        return String.format("procedures/%s", getEntityId());
    }
}
