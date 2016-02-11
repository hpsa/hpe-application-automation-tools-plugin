package com.hp.nga.integrations.dto.pipelines;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.hp.nga.integrations.dto.DTO;
import com.hp.nga.integrations.dto.parameters.ParameterConfig;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: gullery
 * Date: 03/01/15
 * Time: 10:49
 * To change this template use File | Settings | File Templates.
 */

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type")
@JsonSubTypes(@JsonSubTypes.Type(value = PipelineNodeImpl.class, name = "PipelineNodeImpl"))
public interface PipelineNode extends DTO {

	String getCiId();

	PipelineNode setCiId(String ciId);

	String getName();

	PipelineNode setName(String name);

	List<ParameterConfig> getParameters();

	PipelineNode setParameters(List<ParameterConfig> parameters);

	List<PipelinePhase> getPhasesInternal();

	PipelineNode setPhasesInternal(List<PipelinePhase> phasesInternal);

	List<PipelinePhase> getPhasesPostBuild();

	PipelineNode setPhasesPostBuild(List<PipelinePhase> phasesPostBuild);
}
