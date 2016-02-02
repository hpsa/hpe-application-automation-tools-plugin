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

public final class StructureItem {
	private String name;
	private List<ParameterConfig> parameters;
	private List<StructurePhase> phasesInternal;
	private List<StructurePhase> phasesPostBuild;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public List<ParameterConfig> getParameters() {
		return parameters;
	}

	public void setParameters(List<ParameterConfig> parameters) {
		this.parameters = parameters;
	}

	public List<StructurePhase> getPhasesInternal() {
		return phasesInternal;
	}

	public void setPhasesInternal(List<StructurePhase> phasesInternal) {
		this.phasesInternal = phasesInternal;
	}

	public List<StructurePhase> getPhasesPostBuild() {
		return phasesPostBuild;
	}

	public void setPhasesPostBuild(List<StructurePhase> phasesPostBuild) {
		this.phasesPostBuild = phasesPostBuild;
	}
}
