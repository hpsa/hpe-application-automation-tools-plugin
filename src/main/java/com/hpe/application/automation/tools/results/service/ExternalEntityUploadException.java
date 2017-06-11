package com.hpe.application.automation.tools.results.service;

public class ExternalEntityUploadException extends Exception{
	
    private static final long serialVersionUID = -5386355008323770238L;
    
    public ExternalEntityUploadException(Throwable cause) {
        
        super(cause);
    }
    
    public ExternalEntityUploadException(String message) {
        
        super(message);
    }
    
    public ExternalEntityUploadException(String message, Throwable cause) {
        
        super(message, cause);
    }
}
