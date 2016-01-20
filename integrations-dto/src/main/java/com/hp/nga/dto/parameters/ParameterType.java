package com.hp.nga.dto.parameters;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * Created with IntelliJ IDEA.
 * User: gullery
 * Date: 25/08/14
 * Time: 13:07
 * To change this template use File | Settings | File Templates.
 */

public enum ParameterType {
	UNKNOWN("unknown"),
	PASSWORD("password"),
	BOOLEAN("boolean"),
	STRING("string"),
	NUMBER("number"),
	FILE("file"),
	AXIS("axis");

	private String value;

	ParameterType(String value) {
		this.value = value;
	}

	@JsonValue
	public String value() {
		return value;
	}

	@JsonCreator
	public static ParameterType fromValue(String value) {
		if (value == null || value.isEmpty()) {
			throw new IllegalArgumentException("value MUST NOT be null nor empty");
		}

		ParameterType result = UNKNOWN;
		for (ParameterType v : values()) {
			if (v.value.compareTo(value) == 0) {
				result = v;
				break;
			}
		}
		return result;
	}
}
