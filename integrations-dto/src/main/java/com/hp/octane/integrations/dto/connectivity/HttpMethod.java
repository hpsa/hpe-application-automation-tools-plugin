package com.hp.octane.integrations.dto.connectivity;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * Created with IntelliJ IDEA.
 * User: gullery
 * Date: 13/10/14
 * Time: 09:46
 * To change this template use File | Settings | File Templates.
 */

public enum HttpMethod {
	POST("POST"),
	GET("GET"),
	PUT("PUT"),
	DELETE("DELETE");

	private String value;

	HttpMethod(String status) {
		this.value = status;
	}

	@JsonValue
	public String value() {
		return value;
	}

	@JsonCreator
	public static HttpMethod fromValue(String value) {
		if (value == null || value.isEmpty()) {
			throw new IllegalArgumentException("value MUST NOT be null nor empty");
		}

		HttpMethod result = null;
		for (HttpMethod v : values()) {
			if (v.value.equalsIgnoreCase(value)) {
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