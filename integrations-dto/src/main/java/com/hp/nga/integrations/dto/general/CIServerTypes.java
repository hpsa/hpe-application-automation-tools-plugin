package com.hp.nga.integrations.dto.general;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * Created by gullery on 03/01/2016.
 */

public enum CIServerTypes {
	JENKINS("jenkins"),
	TEAMCITY("teamcity"),
	BAMBOO("bamboo"),
	TFS("tfs"),
	UNKNOWN("unknown");

	private String value;

	CIServerTypes(String value) {
		this.value = value;
	}

	@JsonValue
	public String value() {
		return value;
	}

	@JsonCreator
	public static CIServerTypes fromValue(String value) {
		if (value == null || value.isEmpty()) {
			throw new IllegalArgumentException("value MUST NOT be null nor empty");
		}

		CIServerTypes result = UNKNOWN;
		for (CIServerTypes v : values()) {
			if (v.value.compareTo(value) == 0) {
				result = v;
				break;
			}
		}
		return result;
	}
}
