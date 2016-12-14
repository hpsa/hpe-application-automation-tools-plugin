package com.hp.octane.integrations.dto.tests;

import javax.xml.bind.annotation.XmlEnumValue;

public enum TestRunResult {
	@XmlEnumValue(value = "Passed")PASSED("Passed"),
	@XmlEnumValue(value = "Failed")FAILED("Failed"),
	@XmlEnumValue(value = "Skipped")SKIPPED("Skipped");

	private final String value;

	TestRunResult(String value) {
		this.value = value;
	}

	public String value() {
		return value;
	}

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
