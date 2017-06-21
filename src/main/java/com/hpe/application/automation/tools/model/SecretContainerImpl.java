package com.hpe.application.automation.tools.model;

import hudson.util.Secret;

public class SecretContainerImpl implements SecretContainer {
    
    private Secret _secret;
    
    public void initialize(String secret) {
        
        _secret = Secret.fromString(secret);
    }
    
    @Override
    public String toString() {
        
        return _secret.getPlainText();
    }
}
