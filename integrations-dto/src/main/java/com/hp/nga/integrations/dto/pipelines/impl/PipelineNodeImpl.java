package com.hp.nga.integrations.dto.pipelines.impl;

import com.hp.nga.integrations.dto.parameters.CIParameter;
import com.hp.nga.integrations.dto.pipelines.PipelineNode;
import com.hp.nga.integrations.dto.pipelines.PipelinePhase;

import java.util.ArrayList;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: gullery
 * Date: 03/01/15
 * Time: 10:49
 * To change this template use File | Settings | File Templates.
 */

class PipelineNodeImpl implements PipelineNode {
	private String jobCiId;
	private String name;
	private List<CIParameter> parameters = new ArrayList<CIParameter>();
	private List<PipelinePhase> phasesInternal = new ArrayList<PipelinePhase>();
	private List<PipelinePhase> phasesPostBuild = new ArrayList<PipelinePhase>();

	public String getJobCiId() {
		return jobCiId;
	}

	public PipelineNode setJobCiId(String jobCiId) {
		this.jobCiId = jobCiId;
		return this;
	}

	public String getName() {
		return name;
	}

	public PipelineNode setName(String name) {
		this.name = name;
		return this;
	}

	public List<CIParameter> getParameters() {
		return parameters;
	}

	public PipelineNode setParameters(List<CIParameter> parameters) {
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
