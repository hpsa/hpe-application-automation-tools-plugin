package com.hp.application.automation.tools.common.sdk;

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
