package com.hp.nga.integrations.dto.builds;

/**
 * Created with IntelliJ IDEA.
 * User: gullery
 * Date: 25/08/14
 * Time: 13:07
 * To change this template use File | Settings | File Templates.
 */

public enum SnapshotNodeStatus {
	UNAVAILABLE("unavailable"),
	QUEUED("queued"),
	RUNNING("running"),
	FINISHED("finished");

	private String value;

	SnapshotNodeStatus(String value) {
		this.value = value;
	}

	@Override
	public String toString() {
		return value;
	}

	public static SnapshotNodeStatus getByValue(String value) {
		SnapshotNodeStatus result = UNAVAILABLE;
		for (SnapshotNodeStatus i : values()) {
			if (i.value.equals(value)) {
				result = i;
			}
		}
		return result;
	}
}
