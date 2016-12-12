package com.hp.octane.integrations.dto.api.events;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * Created with IntelliJ IDEA.
 * User: gullery
 * Date: 20/10/14
 * Time: 16:39
 * To change this template use File | Settings | File Templates.
 */

public enum CIEventType {
	UNDEFINED("undefined"),
	QUEUED("queued"),
	STARTED("started"),
	FINISHED("finished"),
	SCM("scm");

	private String value;

	CIEventType(String value) {
		this.value = value;
	}

	@JsonValue
	public String value() {
		return value;
	}

	@JsonCreator
	public static CIEventType fromValue(String value) {
		if (value == null || value.isEmpty()) {
			throw new IllegalArgumentException("value MUST NOT be null nor empty");
		}

		CIEventType result = UNDEFINED;
		for (CIEventType v : values()) {
			if (v.value.compareTo(value) == 0) {
				result = v;
				break;
			}
		}
		return result;
	}
}
