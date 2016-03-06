package com.hp.nga.integrations.dto.tests;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum TestRunResult {
	PASSED("Passed"),
	SKIPPED("Skipped"),
	FAILED("Failed");

	private final String value;

	TestRunResult(String value) {
		this.value = value;
	}

	@JsonValue
	public String value() {
		return value;
	}

	@JsonCreator
	public static TestRunResult fromValue(String value) {
		if (value == null || value.isEmpty()) {
			throw new IllegalArgumentException("value MUST NOT be null nor empty");
		}

		TestRunResult result = null;
		for (TestRunResult v : values()) {
			if (v.value.equals(value)) {
				result = v;
				break;
			}
		}

		if (result == null) {
			throw new IllegalStateException("result '" + value + "' is not supported");
		} else {
			return result;
		}
	}
}
