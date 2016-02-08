package com.hp.nga.integrations.dto.projects;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.hp.nga.integrations.dto.parameters.ParameterConfig;

/**
 * Created by lazara on 08/02/2016.
 */
public final class ProjectConfigImpl implements ProjectConfig {
    private String name;
    private ParameterConfig[] parameters;
    private String ciId;

    public void setName(String value) {
        name = value;
    }

    public String getName() {
        return name;
    }

    public void setCiId(String ciId){
        this.ciId= ciId;
    }

    public String getCiId(){
        return ciId;
    }

    public void setParameters(ParameterConfig[] parameters) {
        this.parameters = parameters == null ? null : parameters.clone();
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public ParameterConfig[] getParameters() {
        return parameters == null ? null : parameters.clone();
    }
}
