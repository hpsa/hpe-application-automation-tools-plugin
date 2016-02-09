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

public final class StructureItemImpl implements StructureItem {
	private String ciId;
	private String name;
	private List<ParameterConfig> parameters;
	private List<StructurePhase> phasesInternal;
	private List<StructurePhase> phasesPostBuild;

	public String getCiId() {
		return ciId;
	}

	public StructureItem setCiId(String ciId) {
		this.ciId = ciId;
		return this;
	}

	public String getName() {
		return name;
	}

	public StructureItem setName(String name) {
		this.name = name;
		return this;
	}

	public List<ParameterConfig> getParameters() {
		return parameters;
	}

	public StructureItem setParameters(List<ParameterConfig> parameters) {
		this.parameters = parameters;
		return this;
	}

	public List<StructurePhase> getPhasesInternal() {
		return phasesInternal;
	}

	public StructureItem setPhasesInternal(List<StructurePhase> phasesInternal) {
		this.phasesInternal = phasesInternal;
		return this;
	}

	public List<StructurePhase> getPhasesPostBuild() {
		return phasesPostBuild;
	}

	public StructureItem setPhasesPostBuild(List<StructurePhase> phasesPostBuild) {
		this.phasesPostBuild = phasesPostBuild;
		return this;
	}
}
