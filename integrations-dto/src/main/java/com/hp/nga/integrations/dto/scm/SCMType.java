package com.hp.nga.integrations.dto.scm;

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

	@Override
	public String toString() {
		return value;
	}

	public static SCMType getByValue(String value) {
		SCMType result = UNSUPPORTED;
		for (SCMType i : values()) {
			if (i.value.equals(value)) {
				result = i;
				break;
			}
		}
		return result;
	}
}