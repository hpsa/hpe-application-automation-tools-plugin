package com.hp.nga.integrations.dto.projects;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.hp.nga.integrations.dto.parameters.ParameterConfig;

/**
 * Created by lazara on 08/02/2016.
 */
public interface ProjectConfig {


    public void setName(String value);

    public String getName();

    public void setCiId(String ciId);

    public String getCiId();

    public void setParameters(ParameterConfig[] parameters) ;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public ParameterConfig[] getParameters() ;
}
