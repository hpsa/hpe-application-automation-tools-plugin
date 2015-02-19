package com.hp.octane.plugins.jenkins.model.snapshots;

/**
 * Created with IntelliJ IDEA.
 * User: gullery
 * Date: 25/08/14
 * Time: 13:07
 * To change this template use File | Settings | File Templates.
 */

public enum SnapshotStatus {
	UNAVAILABLE("unavailable"),
	QUEUED("queued"),
	RUNNING("running"),
	FINISHED("finished");

	private String value;

	private SnapshotStatus(String value) {
		this.value = value;
	}

	@Override
	public String toString() {
		return value;
	}

	public static SnapshotStatus getByValue(String value) {
		SnapshotStatus r = null;
		for (SnapshotStatus i : values()) {
			if (i.value.equals(value)) {
				r = i;
				break;
			}
		}
		return r;
	}
}
