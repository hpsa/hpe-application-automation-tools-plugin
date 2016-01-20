package com.hp.nga.integrations.dto.parameters;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * Created by gullery on 19/02/2015.
 */

@JsonIgnoreProperties(ignoreUnknown = true)
public class ParameterConfig {
	private ParameterType type;
	private String name;
	private String description;
	private Object defaultValue;
	private Object[] choices;

	public ParameterConfig() {
	}

	public ParameterConfig(ParameterType type, String name, String description, Object defaultValue, Object[] choices) {
		this.type = type;
		this.name = name;
		this.description = description;
		this.defaultValue = defaultValue;
		this.choices = choices == null ? null : choices.clone();
	}

	public ParameterConfig(ParameterConfig source) {
		if (source == null) {
			throw new IllegalArgumentException("source MUST NOT be null");
		}

		type = source.type;
		name = source.name;
		description = source.description;
		defaultValue = source.defaultValue;
		choices = source.choices == null ? null : source.choices.clone();
	}

	public ParameterType getType() {
		return type;
	}

	public void setType(ParameterType type) {
		this.type = type;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public Object getDefaultValue() {
		return defaultValue;
	}

	public void setDefaultValue(Object defaultValue) {
		this.defaultValue = defaultValue;
	}

	@JsonInclude(JsonInclude.Include.NON_NULL)
	public Object[] getChoices() {
		return choices == null ? null : choices.clone();
	}

	public void setChoices(Object[] choices) {
		this.choices = choices == null ? null : choices.clone();
	}
}
