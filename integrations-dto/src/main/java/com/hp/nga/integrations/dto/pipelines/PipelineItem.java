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

public interface PipelineItem {

	String getCiId();

	PipelineItem setCiId(String ciId);

	String getName();

	PipelineItem setName(String name);

	List<ParameterConfig> getParameters();

	PipelineItem setParameters(List<ParameterConfig> parameters);

	List<PipelinePhase> getPhasesInternal();

	PipelineItem setPhasesInternal(List<PipelinePhase> phasesInternal);

	List<PipelinePhase> getPhasesPostBuild();

	PipelineItem setPhasesPostBuild(List<PipelinePhase> phasesPostBuild);
}
