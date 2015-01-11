package com.hp.octane.plugins.jenkins.model.pipeline;

/**
 * Created with IntelliJ IDEA.
 * User: gullery
 * Date: 25/08/14
 * Time: 13:07
 * To change this template use File | Settings | File Templates.
 */

public enum ParameterType {
	UNAVAILABLE("unavailable"),
	BOOLEAN("boolean"),
	STRING("string"),
	NUMBER("number");

	private String value;

	private ParameterType(String value) {
		this.value = value;
	}

	@Override
	public String toString() {
		return value;
	}

	public static ParameterType getByValue(String value) {
		ParameterType r = null;
		for (ParameterType i : values()) {
			if (i.value.compareTo(value) == 0) {
				r = i;
				break;
			}
		}
		return r;
	}
}
