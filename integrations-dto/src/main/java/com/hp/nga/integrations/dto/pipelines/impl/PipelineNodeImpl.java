package com.hp.nga.integrations.dto.pipelines.impl;

import com.hp.nga.integrations.dto.parameters.ParameterConfig;
import com.hp.nga.integrations.dto.pipelines.PipelineNode;
import com.hp.nga.integrations.dto.pipelines.PipelinePhase;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: gullery
 * Date: 03/01/15
 * Time: 10:49
 * To change this template use File | Settings | File Templates.
 */

class PipelineNodeImpl implements PipelineNode {
	private String ciId;
	private String name;
	private List<ParameterConfig> parameters;
	private List<PipelinePhase> phasesInternal;
	private List<PipelinePhase> phasesPostBuild;

	public String getCiId() {
		return ciId;
	}

	public PipelineNode setCiId(String ciId) {
		this.ciId = ciId;
		return this;
	}

	public String getName() {
		return name;
	}

	public PipelineNode setName(String name) {
		this.name = name;
		return this;
	}

	public List<ParameterConfig> getParameters() {
		return parameters;
	}

	public PipelineNode setParameters(List<ParameterConfig> parameters) {
		this.parameters = parameters;
		return this;
	}

	public List<PipelinePhase> getPhasesInternal() {
		return phasesInternal;
	}

	public PipelineNode setPhasesInternal(List<PipelinePhase> phasesInternal) {
		this.phasesInternal = phasesInternal;
		return this;
	}

	public List<PipelinePhase> getPhasesPostBuild() {
		return phasesPostBuild;
	}

	public PipelineNode setPhasesPostBuild(List<PipelinePhase> phasesPostBuild) {
		this.phasesPostBuild = phasesPostBuild;
		return this;
	}
}
