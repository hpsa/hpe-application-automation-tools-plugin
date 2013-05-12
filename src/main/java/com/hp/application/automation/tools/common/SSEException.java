package com.hp.application.automation.tools.common;

public class SSEException extends RuntimeException {
    
    private static final long serialVersionUID = -5386355008323770858L;
    
    public SSEException(Throwable cause) {
        
        super(cause);
    }
    
    public SSEException(String message) {
        
        super(message);
    }
}
