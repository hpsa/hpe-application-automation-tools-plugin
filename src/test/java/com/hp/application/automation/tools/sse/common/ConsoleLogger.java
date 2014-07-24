package com.hp.application.automation.tools.sse.common;

import com.hp.application.automation.tools.sse.sdk.Logger;

public class ConsoleLogger implements Logger {
    
    @Override
    public void log(String message) {
        
        System.out.println(message);
    }
    
}
