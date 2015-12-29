package com.hp.octane.plugins.common.model.api;

import com.hp.octane.plugins.jetbrains.teamcity.model.api.ParameterConfig;

/**
 * Created by lazara on 24/12/2015.
 */
public class ProjectConfig {

    public ProjectConfig(String buildName, String externalId, ParameterConfig[] parameters) {
        this.name=buildName;
        this.id=externalId;
        this.parameters = parameters;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public ParameterConfig[] getParameters() {
        return parameters;
    }

    private String name;
    private String id;
    private ParameterConfig[] parameters;

}
