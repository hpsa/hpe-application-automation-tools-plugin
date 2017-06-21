package com.hpe.application.automation.tools.sse.sdk;

/**
 * 
 * @author Amir Zahavi
 * 
 */
public class ConsoleLogger implements Logger {
    
    @Override
    public void log(String message) {
        
        System.out.println(message);
    }
}
