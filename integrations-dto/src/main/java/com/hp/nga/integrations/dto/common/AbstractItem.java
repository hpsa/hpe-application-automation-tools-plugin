package com.hp.nga.integrations.dto.common;

import com.hp.nga.integrations.dto.parameters.ParameterConfig;

/**
 * Created with IntelliJ IDEA.
 * User: gullery
 * Date: 12/01/15
 * Time: 10:46
 * To change this template use File | Settings | File Templates.
 */

public abstract class AbstractItem<TP extends ParameterConfig, TPH extends AbstractPhase> {
	private String name;
	private TP[] parameters;
	private TPH[] internals;
	private TPH[] postBuilds;

	private void setName(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

	protected void setParameters(TP[] parameters) {
		this.parameters = parameters;
	}

	public TP[] getParameters() {
		return parameters;
	}

	protected void setInternals(TPH[] internals) {
		this.internals = internals;
	}

	public TPH[] getInternals() {
		return internals;
	}

	protected void setPostBuilds(TPH[] postBuilds) {
		this.postBuilds = postBuilds;
	}

	public TPH[] getPostBuilds() {
		return postBuilds;
	}
}
