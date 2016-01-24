package com.hp.nga.integrations.dto.snapshots;

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

	SnapshotStatus(String value) {
		this.value = value;
	}

	@Override
	public String toString() {
		return value;
	}

	public static SnapshotStatus getByValue(String value) {
		for (SnapshotStatus i : values()) {
			if (i.value.equals(value)) {
				return i;
			}
		}
		throw new RuntimeException("No SnapshotStatus matches '" + value + "'");
	}
}
