package com.hp.nga.integrations.dto.pipelines;

import com.hp.nga.integrations.dto.parameters.ParameterConfig;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: gullery
 * Date: 03/01/15
 * Time: 10:49
 * To change this template use File | Settings | File Templates.
 */

public interface PipelineNode {

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
