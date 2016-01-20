package com.hp.nga.integrations.dto.builds;

/**
 * Created with IntelliJ IDEA.
 * User: gullery
 * Date: 25/08/14
 * Time: 13:07
 * To change this template use File | Settings | File Templates.
 */

public enum SnapshotNodeResult {
	UNAVAILABLE("unavailable"),
	UNSTABLE("unstable"),
	ABORTED("aborted"),
	FAILURE("failure"),
	SUCCESS("success");

	private String value;

	SnapshotNodeResult(String value) {
		this.value = value;
	}

	@Override
	public String toString() {
		return value;
	}

	public static SnapshotNodeResult getByValue(String value) {
		SnapshotNodeResult result = UNAVAILABLE;
		for (SnapshotNodeResult i : values()) {
			if (i.value.equals(value)) {
				result = i;
			}
		}
		return result;
	}
}
