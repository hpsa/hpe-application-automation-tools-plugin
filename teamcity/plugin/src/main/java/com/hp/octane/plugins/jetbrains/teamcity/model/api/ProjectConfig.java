package com.hp.octane.plugins.jetbrains.teamcity.model.api;

/**
 * Created by lazara on 24/12/2015.
 */
public class ProjectConfig {

    public ProjectConfig(String buildName, String externalId) {
        this.name=buildName;
        this.id=externalId;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setParameters(ParameterConfig[] parameters) {
        this.parameters = parameters;
    }

    public ParameterConfig[] getParameters() {
        return parameters;
    }

    private String name;
    private String id;
    private ParameterConfig[] parameters;

}
