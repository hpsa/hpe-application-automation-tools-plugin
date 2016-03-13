package com.hp.nga.integrations.dto.general.impl;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.hp.nga.integrations.dto.general.CIJobMetadata;
import com.hp.nga.integrations.dto.parameters.CIParameter;

/**
 * Created by lazara on 08/02/2016.
 */

@JsonIgnoreProperties(ignoreUnknown = true)
class CIJobMetadataImpl implements CIJobMetadata {
	private String name;
	private CIParameter[] parameters;
	private String ciId;

	public CIJobMetadata setName(String value) {
		name = value;
		return this;
	}

	public String getName() {
		return name;
	}

	public CIJobMetadata setCiId(String ciId) {
		this.ciId = ciId;
		return this;
	}

	public String getCiId() {
		return ciId;
	}

	public CIJobMetadata setParameters(CIParameter[] parameters) {
		this.parameters = parameters == null ? null : parameters.clone();
		return this;
	}

	@JsonInclude(JsonInclude.Include.NON_NULL)
	public CIParameter[] getParameters() {
		return parameters == null ? null : parameters.clone();
	}
}
