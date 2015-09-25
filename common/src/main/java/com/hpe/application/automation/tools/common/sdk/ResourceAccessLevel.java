package com.hpe.application.automation.tools.common.sdk;

import com.hpe.application.automation.tools.common.rest.HttpHeaders;

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
