package com.hpe.application.automation.tools.results.service;

public class AlmRestException extends Exception{
	
    private static final long serialVersionUID = -5386355008323770238L;
    
    public AlmRestException(Throwable cause) {
        
        super(cause);
    }
    
    public AlmRestException(String message) {
        
        super(message);
    }
    
    public AlmRestException(String message, Throwable cause) {
        
        super(message, cause);
    }
}
