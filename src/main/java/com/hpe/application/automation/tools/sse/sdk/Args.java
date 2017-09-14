package com.hpe.application.automation.tools.sse.sdk;

import com.hpe.application.automation.tools.model.CdaDetails;

/**
 * 
 * @author Effi Bar-She'an
 * @author Dani Schreiber
 * 
 */
public class Args {
    
    private final String _url;
    private final String _domain;
    private final String _project;
    private final String _username;
    private final String _password;
    private final String _runType;
    private final String _entityId;
    private final String _duration;
    private final String _description;
    private final String _postRunAction;
    private final String _environmentConfigurationId;
    
    private final CdaDetails _cdaDetails;
    
    public Args(
            String url,
            String domain,
            String project,
            String username,
            String password,
            String runType,
            String entityId,
            String duration,
            String description,
            String postRunAction,
            String environmentConfigurationId,
            CdaDetails cdaDetails) {
        
        _url = url;
        _domain = domain;
        _project = project;
        _username = username;
        _password = password;
        _entityId = entityId;
        _runType = runType;
        _duration = duration;
        _description = description;
        _postRunAction = postRunAction;
        _environmentConfigurationId = environmentConfigurationId;
        _cdaDetails = cdaDetails;
    }
    
    public String getUrl() {
        
        return _url;
    }
    
    public String getDomain() {
        
        return _domain;
    }
    
    public String getProject() {
        
        return _project;
    }
    
    public String getUsername() {
        
        return _username;
    }
    
    public String getPassword() {
        
        return _password;
    }
    
    public String getEntityId() {
        
        return _entityId;
    }
    
    public String getRunType() {
        return _runType;
    }
    
    public String getDuration() {
        
        return _duration;
    }
    
    public String getDescription() {
        
        return _description;
    }
    
    public String getPostRunAction() {
        
        return _postRunAction;
    }
    
    public String getEnvironmentConfigurationId() {
        
        return _environmentConfigurationId;
    }
    
    public CdaDetails getCdaDetails() {
        
        return _cdaDetails;
    }
}
