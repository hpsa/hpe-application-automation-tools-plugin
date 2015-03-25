package com.hp.octane.plugins.jenkins.model.causes;

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
	UPSTREAM("upstream");

	private String value;

	private CIEventCauseType(String value) {
		this.value = value;
	}

	@Override
	public String toString() {
		return value;
	}

	public static CIEventCauseType getByValue(String value) {
		for (CIEventCauseType v : values()) {
			if (v.value.equals(value)) {
				return v;
			}
		}
		throw new RuntimeException("No CIEventCauseType matches '" + value + "'");
	}
}
