package com.hp.application.automation.tools.model;

public class SecretContainerTest implements SecretContainer {
    
    private String _secret;
    
    public void initialize(String secret) {
        
        _secret = secret;
    }
    
    @Override
    public String toString() {
        
        return _secret;
    }
}
