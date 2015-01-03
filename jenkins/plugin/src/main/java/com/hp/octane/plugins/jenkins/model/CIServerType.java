package com.hp.octane.plugins.jenkins.model;

/**
 * Created with IntelliJ IDEA.
 * User: gullery
 * Date: 25/08/14
 * Time: 13:07
 * To change this template use File | Settings | File Templates.
 */
public enum CIServerType {
	UNDEFINED("undefined"),
	JENKINS("jenkins"),
	HUDSON("hudson"),
	TFS("tfs");

	private String value;

	private CIServerType(String value) {
		this.value = value;
	}

	@Override
	public String toString() {
		return value;
	}

	public static CIServerType getByValue(String type) {
		CIServerType r = null;
		for (CIServerType i : values()) {
			if (i.value.equals(type)) {
				r = i;
				break;
			}
		}
		return r;
	}
}
