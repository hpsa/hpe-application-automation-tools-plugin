package com.hp.octane.plugins.jenkins.model.scm;

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

	private SCMType(String status) {
		this.value = status;
	}

	@Override
	public String toString() {
		return value;
	}

	public static SCMType getByValue(String value) {
		SCMType r = null;
		for (SCMType i : values()) {
			if (i.value.compareTo(value) == 0) {
				r = i;
				break;
			}
		}
		return r;
	}
}