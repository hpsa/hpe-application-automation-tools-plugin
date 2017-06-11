package com.hpe.application.automation.tools.results.parser;

public class ReportParseException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
    public ReportParseException(){
    	
    }
    
    public ReportParseException(Throwable cause) {
        
        super(cause);
    }
    
    public ReportParseException(String message) {
        
        super(message);
    }
    
    public ReportParseException(String message, Throwable cause) {
        
        super(message, cause);
    }	

}
