package com.hp.nga.integrations.dto.parameters.impl;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.hp.nga.integrations.dto.parameters.CIParameter;
import com.hp.nga.integrations.dto.parameters.CIParameterType;

/**
 * Created by gullery on 19/02/2015.
 * <p/>
 * Default implementation of CI Parameter DTO
 */

@JsonIgnoreProperties(ignoreUnknown = true)
class CIParameterImpl implements CIParameter {
	private CIParameterType type;
	private String name;
	private String description;
	private Object[] choices;
	private Object defaultValue;
	private Object value;

	public CIParameterType getType() {
		return type;
	}

	public CIParameter setType(CIParameterType type) {
		this.type = type;
		return this;
	}

	public String getName() {
		return name;
	}

	public CIParameter setName(String name) {
		this.name = name;
		return this;
	}

	public String getDescription() {
		return description;
	}

	public CIParameter setDescription(String description) {
		this.description = description;
		return this;
	}

	@JsonInclude(JsonInclude.Include.NON_NULL)
	public Object[] getChoices() {
		return choices;
	}

	public CIParameter setChoices(Object[] choices) {
		this.choices = choices;
		return this;
	}

	public Object getDefaultValue() {
		return defaultValue;
	}

	public CIParameter setDefaultValue(Object defaultValue) {
		this.defaultValue = defaultValue;
		return this;
	}

	@JsonInclude(JsonInclude.Include.NON_NULL)
	public Object getValue() {
		return value;
	}

	public CIParameter setValue(Object value) {
		this.value = value;
		return this;
	}
}
