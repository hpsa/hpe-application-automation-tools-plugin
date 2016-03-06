package com.hp.nga.integrations.dto.causes;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * Created with IntelliJ IDEA.
 * User: gullery
 * Date: 25/08/14
 * Time: 13:07
 * To change this template use File | Settings | File Templates.
 */

public enum CIEventCauseType {
	SCM("scm"),
	USER("user"),
	TIMER("timer"),
	UPSTREAM("upstream"),
	UNDEFINED("undefined");

	private String value;

	CIEventCauseType(String value) {
		this.value = value;
	}

	@JsonValue
	public String value() {
		return value;
	}

	@JsonCreator
	public static CIEventCauseType fromValue(String value) {
		if (value == null || value.isEmpty()) {
			throw new IllegalArgumentException("value MUST NOT be null nor empty");
		}

		CIEventCauseType result = UNDEFINED;
		for (CIEventCauseType v : values()) {
			if (v.value.compareTo(value) == 0) {
				result = v;
				break;
			}
		}
		return result;
	}
}
