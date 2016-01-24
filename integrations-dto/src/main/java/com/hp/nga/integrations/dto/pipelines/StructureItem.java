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
	private List<StructurePhase> internals;
	private List<StructurePhase> postBuilds;

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

	public List<StructurePhase> getInternals() {
		return internals;
	}

	public void setInternals(List<StructurePhase> internals) {
		this.internals = internals;
	}

	public List<StructurePhase> getPostBuilds() {
		return postBuilds;
	}

	public void setPostBuilds(List<StructurePhase> postBuilds) {
		this.postBuilds = postBuilds;
	}
}
