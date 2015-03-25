package com.hp.octane.plugins.jenkins.model.events;

/**
 * Created with IntelliJ IDEA.
 * User: gullery
 * Date: 20/10/14
 * Time: 16:39
 * To change this template use File | Settings | File Templates.
 */

public enum CIEventType {
	QUEUED("queued"),
	STARTED("started"),
	FINISHED("finished");

	private String value;

	private CIEventType(String value) {
		this.value = value;
	}

	@Override
	public String toString() {
		return value;
	}

	public static CIEventType getByValue(String value) {
		for (CIEventType v : values()) {
			if (v.value.equals(value)) {
				return v;
			}
		}
		throw new RuntimeException("No CIEventType matches '" + value + "'");
	}
}
