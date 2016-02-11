package com.hp.nga.integrations.dto.pipelines;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.hp.nga.integrations.dto.DTO;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: gullery
 * Date: 08/01/15
 * Time: 23:15
 * To change this template use File | Settings | File Templates.
 */

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type")
@JsonSubTypes(@JsonSubTypes.Type(value = PipelinePhaseImpl.class, name = "PipelinePhaseImpl"))
public interface PipelinePhase extends DTO {

	String getName();

	PipelinePhase setName(String name);

	boolean isBlocking();

	PipelinePhase setBlocking(boolean blocking);

	List<PipelineNode> getJobs();

	PipelinePhase setJobs(List<PipelineNode> jobs);
}