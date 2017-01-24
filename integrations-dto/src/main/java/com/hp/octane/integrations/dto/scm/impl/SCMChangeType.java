package com.hp.octane.integrations.dto.scm.impl;


import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum SCMChangeType {
	ADD("add"),
	EDIT("edit"),
	DELETE("delete"),
	UNKNOWN("unknown");

	private String value;

	SCMChangeType(String status) {
		this.value = status;
	}

	@JsonValue
	public String value() {
		return value;
	}

	@JsonCreator
	public static SCMChangeType fromValue(String value) {
		if (value == null || value.isEmpty()) {
			throw new IllegalArgumentException("value MUST NOT be null nor empty");
		}

		SCMChangeType result = UNKNOWN;
		for (SCMChangeType v : values()) {
			if (v.value.compareTo(value) == 0) {
				result = v;
				break;
			}
		}
		return result;
	}
}