package com.hpe.application.automation.tools.common.model;

import java.util.List;

/**
 * Created by mprilepina on 14/08/2015.
 */
public class AutEnvironmentConfigModel {

    private String almServerUrl;
    private final String almUserName;
    private final String almPassword;
    private final String almDomain;
    private final String almProject;

    private final boolean useExistingAutEnvConf;

    private final String autEnvID;
    private final String autEnvConf;

    private final String pathToJsonFile;
    private List<AutEnvironmentParameterModel> autEnvironmentParameters;

    private String currentConfigID;

    public AutEnvironmentConfigModel(String almServerUrl,
                                     String almUserName,
                                     String almPassword,
                                     String almDomain,
                                     String almProject,
                                     boolean useExistingAutEnvConf,
                                     String autEnvID,
                                     String envConf,
                                     String pathToJsonFile,
                                     List<AutEnvironmentParameterModel> autEnvironmentParameters){
        this.almUserName = almUserName;
        this.almPassword = almPassword;
        this.almDomain = almDomain;
        this.almProject = almProject;
        this.useExistingAutEnvConf = useExistingAutEnvConf;
        this.autEnvID = autEnvID;
        this.autEnvConf = envConf;
        this.pathToJsonFile = pathToJsonFile;
        this.almServerUrl = almServerUrl;
        this.autEnvironmentParameters = autEnvironmentParameters;
    }

    public String getAlmServerUrl() {
        return almServerUrl;
    }

    public String getAlmUserName() {
        return almUserName;
    }

    public String getAlmPassword() {
        return almPassword;
    }

    public String getAlmDomain() {
        return almDomain;
    }

    public String getAlmProject() {
        return almProject;
    }

    public boolean isUseExistingAutEnvConf() {
        return useExistingAutEnvConf;
    }

    public String getAutEnvID() {
        return autEnvID;
    }

    public String getAutEnvConf() {
        return autEnvConf;
    }


    public List<AutEnvironmentParameterModel> getAutEnvironmentParameters() {
        return autEnvironmentParameters;
    }

    public String getPathToJsonFile() {
        return pathToJsonFile;
    }


    public String getCurrentConfigID() {
        return currentConfigID;
    }

    public void setCurrentConfigID(String currentConfigID) {
        this.currentConfigID = currentConfigID;
    }

}
