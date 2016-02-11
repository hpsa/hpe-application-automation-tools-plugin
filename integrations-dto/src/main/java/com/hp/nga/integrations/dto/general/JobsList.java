package com.hp.nga.integrations.dto.general;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.hp.nga.integrations.dto.DTO;

/**
 * Created by gullery on 06/01/2016.
 */

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type")
@JsonSubTypes(@JsonSubTypes.Type(value = JobsListImpl.class, name = "JobsListImpl"))
public interface JobsList extends DTO {

	void setJobs(JobConfig[] jobs) ;

	JobConfig[] getJobs();
}
