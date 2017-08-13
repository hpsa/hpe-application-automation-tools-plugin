package com.hpe.application.automation.tools.model;

import java.util.List;

/**
 * Created by barush
 */
public class AUTEnvironmentResolvedModel {
    
    private String almServerName;
    private String almServerUrl;
    private String almUserName;
    private String almPassword;
    private String almDomain;
    private String almProject;
    
    private String autEnvironmentId;
    private boolean useExistingAutEnvConf;
    private String existingAutEnvConfId;
    private boolean createNewAutEnvConf;
    private String newAutEnvConfName;
    
    private List<AutEnvironmentParameterModel> autEnvironmentParameters;
    
    private String pathToJsonFile;
    private String outputParameter;
    
    public AUTEnvironmentResolvedModel(
            String almServerName,
            String almServerUrl,
            String almUserName,
            String almPassword,
            String almDomain,
            String almProject,
            String autEnvironmentId,
            boolean useExistingAutEnvConf,
            String existingAutEnvConfId,
            boolean createNewAutEnvConf,
            String newAutEnvConfName,
            List<AutEnvironmentParameterModel> autEnvironmentParameters,
            String pathToJsonFile,
            String outputParameter) {
        
        this.almServerName = almServerName;
        this.almServerUrl = almServerUrl;
        this.almUserName = almUserName;
        this.almPassword = almPassword;
        this.almDomain = almDomain;
        this.almProject = almProject;
        this.autEnvironmentId = autEnvironmentId;
        this.useExistingAutEnvConf = useExistingAutEnvConf;
        this.existingAutEnvConfId = existingAutEnvConfId;
        this.createNewAutEnvConf = createNewAutEnvConf;
        this.newAutEnvConfName = newAutEnvConfName;
        this.autEnvironmentParameters = autEnvironmentParameters;
        this.pathToJsonFile = pathToJsonFile;
        this.outputParameter = outputParameter;
    }
    
    public String getAlmServerName() {
        return almServerName;
    }
    
    public String getAlmServerUrl() {
        return almServerUrl;
    }
    
    public String getAlmUserName() {
        return almUserName;
    }
    
    public String getAlmPassword() {
        return almPassword.toString();
    }
    
    public String getAlmDomain() {
        return almDomain;
    }
    
    public String getAlmProject() {
        return almProject;
    }
    
    public String getAutEnvironmentId() {
        return autEnvironmentId;
    }
    
    public boolean isUseExistingAutEnvConf() {
        return useExistingAutEnvConf;
    }
    
    public String getExistingAutEnvConfId() {
        return existingAutEnvConfId;
    }
    
    public boolean isCreateNewAutEnvConf() {
        return createNewAutEnvConf;
    }
    
    public String getNewAutEnvConfName() {
        return newAutEnvConfName;
    }
    
    public List<AutEnvironmentParameterModel> getAutEnvironmentParameters() {
        return autEnvironmentParameters;
    }
    
    public String getPathToJsonFile() {
        return pathToJsonFile;
    }
    
    public String getOutputParameter() {
        return outputParameter;
    }
}
