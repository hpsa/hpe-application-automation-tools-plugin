package com.hp.octane.plugins.jenkins.model.parameters;

/**
 * Created with IntelliJ IDEA.
 * User: gullery
 * Date: 25/08/14
 * Time: 13:07
 * To change this template use File | Settings | File Templates.
 */

public enum ParameterType {
	UNAVAILABLE("unavailable"),
	PASSWORD("password"),
	BOOLEAN("boolean"),
	STRING("string"),
	NUMBER("number"),
	FILE("file");

	private String value;

	ParameterType(String value) {
		this.value = value;
	}

	@Override
	public String toString() {
		return value;
	}

	public static ParameterType getByValue(String value) {
		for (ParameterType i : values()) {
			if (i.value.equals(value)) {
				return i;
			}
		}
		throw new RuntimeException("No ParameterType matches '" + value + "'");
	}
}
