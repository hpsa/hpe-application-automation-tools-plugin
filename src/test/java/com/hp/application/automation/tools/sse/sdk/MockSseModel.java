package com.hp.application.automation.tools.sse.sdk;

import com.hp.application.automation.tools.model.CdaDetails;
import com.hp.application.automation.tools.model.ProxySettings;
import com.hp.application.automation.tools.model.SecretContainer;
import com.hp.application.automation.tools.model.SecretContainerTest;
import com.hp.application.automation.tools.model.SseModel;

public class MockSseModel extends SseModel {
    
    public MockSseModel(
            String almServerName,
            String almUserName,
            String almPassword,
            String almDomain,
            String almProject,
            String runType,
            String almEntityId,
            String timeslotDuration,
            String description,
            String postRunAction,
            String environmentConfigurationId,
            CdaDetails cdaDetails,
            ProxySettings proxySettings) {
        
        super(
                almServerName,
                almUserName,
                almPassword,
                almDomain,
                almProject,
                runType,
                almEntityId,
                timeslotDuration,
                description,
                postRunAction,
                environmentConfigurationId,
                cdaDetails,
                null);
    }
    
    @Override
    protected SecretContainer setPassword(String almPassword) {
        
        SecretContainer secretContainer = new SecretContainerTest();
        secretContainer.initialize(almPassword);
        
        return secretContainer;
    }
}
