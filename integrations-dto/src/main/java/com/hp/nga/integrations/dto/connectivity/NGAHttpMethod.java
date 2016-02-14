package com.hp.nga.integrations.dto.connectivity;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * Created with IntelliJ IDEA.
 * User: gullery
 * Date: 13/10/14
 * Time: 09:46
 * To change this template use File | Settings | File Templates.
 */

public enum NGAHttpMethod {
	POST("post"),
	GET("get"),
	PUT("put"),
	DELETE("delete");

	private String value;

	NGAHttpMethod(String status) {
		this.value = status;
	}

	@JsonValue
	public String value() {
		return value;
	}

	@JsonCreator
	public static NGAHttpMethod fromValue(String value) {
		if (value == null || value.isEmpty()) {
			throw new IllegalArgumentException("value MUST NOT be null nor empty");
		}

		NGAHttpMethod result = null;
		for (NGAHttpMethod v : values()) {
			if (v.value.compareTo(value) == 0) {
				result = v;
				break;
			}
		}
		if (result == null) {
			throw new IllegalStateException("method '" + value + "' is not supported");
		} else {
			return result;
		}
	}
}