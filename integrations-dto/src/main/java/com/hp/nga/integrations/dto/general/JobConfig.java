package com.hp.nga.integrations.dto.general;

import com.hp.nga.integrations.dto.parameters.ParameterConfig;

/**
 * Created by lazara on 08/02/2016.
 */
public interface JobConfig {

    JobConfig setName(String value);

    String getName();

    JobConfig setCiId(String ciId);

    String getCiId();

    JobConfig setParameters(ParameterConfig[] parameters) ;

    ParameterConfig[] getParameters() ;
}
