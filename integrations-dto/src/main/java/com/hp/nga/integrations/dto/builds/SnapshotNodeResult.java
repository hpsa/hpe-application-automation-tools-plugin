package com.hp.nga.integrations.dto.builds;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

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

	@JsonValue
	public String value() {
		return value;
	}

	@JsonCreator
	public static SnapshotNodeResult fromValue(String value) {
		if (value != null) {
			for (SnapshotNodeResult v : values()) {
				if (v.value.equals(value)) {
					return v;
				}
			}
		}
		return UNAVAILABLE;
	}
}
