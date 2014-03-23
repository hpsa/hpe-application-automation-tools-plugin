package com.hp.application.automation.tools.sse.sdk;

import com.hp.application.automation.tools.rest.HttpHeaders;

public enum ResourceAccessLevel {
    PUBLIC(null), PROTECTED(HttpHeaders.PtaL), PRIVATE(HttpHeaders.PvaL);
    
    private String _headerName;
    
    private ResourceAccessLevel(String headerName) {
        
        _headerName = headerName;
    }
    
    public String getUserHeaderName() {
        
        return _headerName;
    }
}
