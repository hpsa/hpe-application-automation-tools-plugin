package com.hp.nga.integrations.dto.general;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.hp.nga.integrations.dto.parameters.ParameterConfig;

/**
 * Created by lazara on 08/02/2016.
 */
public final class JobConfigImpl implements JobConfig {
	private String name;
	private ParameterConfig[] parameters;
	private String ciId;

	public JobConfig setName(String value) {
		name = value;
		return this;
	}

	public String getName() {
		return name;
	}

	public JobConfig setCiId(String ciId) {
		this.ciId = ciId;
		return this;
	}

	public String getCiId() {
		return ciId;
	}

	public JobConfig setParameters(ParameterConfig[] parameters) {
		this.parameters = parameters == null ? null : parameters.clone();
		return this;
	}

	@JsonInclude(JsonInclude.Include.NON_NULL)
	public ParameterConfig[] getParameters() {
		return parameters == null ? null : parameters.clone();
	}
}
