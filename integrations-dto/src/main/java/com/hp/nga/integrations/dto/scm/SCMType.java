package com.hp.nga.integrations.dto.scm;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * Created with IntelliJ IDEA.
 * User: gullery
 * Date: 13/10/14
 * Time: 09:46
 * To change this template use File | Settings | File Templates.
 */

public enum SCMType {
	UNSUPPORTED("unsupported"),
	GIT("git");

	private String value;

	SCMType(String status) {
		this.value = status;
	}

	@JsonValue
	public String value() {
		return value;
	}

	@JsonCreator
	public static SCMType fromValue(String value) {
		if (value == null || value.isEmpty()) {
			throw new IllegalArgumentException("value MUST NOT be null nor empty");
		}

		SCMType result = UNSUPPORTED;
		for (SCMType v : values()) {
			if (v.value.compareTo(value) == 0) {
				result = v;
				break;
			}
		}
		return result;
	}
}