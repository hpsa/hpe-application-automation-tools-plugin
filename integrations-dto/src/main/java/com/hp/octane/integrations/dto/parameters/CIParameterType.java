package com.hp.octane.integrations.dto.parameters;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * Created with IntelliJ IDEA.
 * User: gullery
 * Date: 25/08/14
 * Time: 13:07
 * To change this template use File | Settings | File Templates.
 */

public enum CIParameterType {
	UNKNOWN("unknown"),
	PASSWORD("password"),
	BOOLEAN("boolean"),
	STRING("string"),
	NUMBER("number"),
	FILE("file"),
	AXIS("axis");

	private String value;

	CIParameterType(String value) {
		this.value = value;
	}

	@JsonValue
	public String value() {
		return value;
	}

	@JsonCreator
	public static CIParameterType fromValue(String value) {
		if (value == null || value.isEmpty()) {
			throw new IllegalArgumentException("value MUST NOT be null nor empty");
		}

		CIParameterType result = UNKNOWN;
		for (CIParameterType v : values()) {
			if (v.value.compareTo(value) == 0) {
				result = v;
				break;
			}
		}
		return result;
	}
}
