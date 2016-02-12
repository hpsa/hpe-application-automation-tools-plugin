package com.hp.nga.integrations.dto.general.impl;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.hp.nga.integrations.dto.general.CIJobConfig;
import com.hp.nga.integrations.dto.parameters.ParameterConfig;

/**
 * Created by lazara on 08/02/2016.
 */

@JsonIgnoreProperties(ignoreUnknown = true)
class CIJobConfigImpl implements CIJobConfig {
	private String name;
	private ParameterConfig[] parameters;
	private String ciId;

	public CIJobConfig setName(String value) {
		name = value;
		return this;
	}

	public String getName() {
		return name;
	}

	public CIJobConfig setCiId(String ciId) {
		this.ciId = ciId;
		return this;
	}

	public String getCiId() {
		return ciId;
	}

	public CIJobConfig setParameters(ParameterConfig[] parameters) {
		this.parameters = parameters == null ? null : parameters.clone();
		return this;
	}

	@JsonInclude(JsonInclude.Include.NON_NULL)
	public ParameterConfig[] getParameters() {
		return parameters == null ? null : parameters.clone();
	}
}
