package com.hp.octane.integrations.dto.api.pipelines;

import com.hp.octane.integrations.dto.DTOBase;
import com.hp.octane.integrations.dto.api.parameters.CIParameter;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: gullery
 * Date: 03/01/15
 * Time: 10:49
 * To change this template use File | Settings | File Templates.
 */

public interface PipelineNode extends DTOBase {

	String getJobCiId();

	PipelineNode setJobCiId(String jobCiId);

	String getName();

	PipelineNode setName(String name);

	List<CIParameter> getParameters();

	PipelineNode setParameters(List<CIParameter> parameters);

	List<PipelinePhase> getPhasesInternal();

	PipelineNode setPhasesInternal(List<PipelinePhase> phasesInternal);

	List<PipelinePhase> getPhasesPostBuild();

	PipelineNode setPhasesPostBuild(List<PipelinePhase> phasesPostBuild);
}
