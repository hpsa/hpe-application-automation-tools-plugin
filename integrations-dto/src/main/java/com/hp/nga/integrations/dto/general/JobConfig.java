package com.hp.nga.integrations.dto.general;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.hp.nga.integrations.dto.DTO;
import com.hp.nga.integrations.dto.parameters.ParameterConfig;

/**
 * Created by lazara on 08/02/2016.
 */

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type")
@JsonSubTypes(@JsonSubTypes.Type(value = JobConfigImpl.class, name = "JobConfigImpl"))
public interface JobConfig extends DTO {

	JobConfig setName(String value);

	String getName();

	JobConfig setCiId(String ciId);

	String getCiId();

	JobConfig setParameters(ParameterConfig[] parameters);

	ParameterConfig[] getParameters();
}
