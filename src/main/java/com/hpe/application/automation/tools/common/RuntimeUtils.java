package com.hpe.application.automation.tools.common;

public class RuntimeUtils {
    
    @SuppressWarnings("unchecked")
    public static <T> T cast(Object obj) {
        
        return (T) obj;
    }
}
