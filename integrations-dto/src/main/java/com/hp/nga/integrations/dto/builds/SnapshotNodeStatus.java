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

	@JsonValue
	public String value() {
		return value;
	}

	@JsonCreator
	public static SnapshotNodeStatus fromValue(String value) {
		if (value != null) {
			for (SnapshotNodeStatus v : values()) {
				if (v.value.equals(value)) {
					return v;
				}
			}
		}
		return UNAVAILABLE;
	}
}
