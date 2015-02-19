package com.hp.octane.plugins.jenkins.model.snapshots;

/**
 * Created with IntelliJ IDEA.
 * User: gullery
 * Date: 25/08/14
 * Time: 13:07
 * To change this template use File | Settings | File Templates.
 */

public enum SnapshotResult {
	UNAVAILABLE("unavailable"),
	UNSTABLE("unstable"),
	ABORTED("aborted"),
	FAILURE("failure"),
	SUCCESS("success");

	private String value;

	private SnapshotResult(String value) {
		this.value = value;
	}

	@Override
	public String toString() {
		return value;
	}

	public static SnapshotResult getByValue(String value) {
		SnapshotResult r = null;
		for (SnapshotResult i : values()) {
			if (i.value.compareTo(value) == 0) {
				r = i;
				break;
			}
		}
		return r;
	}
}
