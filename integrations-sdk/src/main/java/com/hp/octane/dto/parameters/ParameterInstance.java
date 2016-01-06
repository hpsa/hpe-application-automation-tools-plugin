package com.hp.octane.dto.parameters;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Created by gullery on 19/02/2015.
 */

@JsonIgnoreProperties(ignoreUnknown = true)
public class ParameterInstance extends ParameterConfig {
	private Object value;

	public ParameterInstance() {
	}

	public ParameterInstance(ParameterConfig pc, String value) {
		super(pc);
		this.value = value;
	}

	public Object getValue() {
		return value;
	}

	public void setValue(Object value) {
		this.value = value;
	}
}
