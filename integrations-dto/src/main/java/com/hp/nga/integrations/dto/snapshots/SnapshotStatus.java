package com.hp.nga.integrations.dto.snapshots;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

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

	@JsonValue
	public String value() {
		return value;
	}

	@JsonCreator
	public static SnapshotStatus fromValue(String value) {
		if (value == null || value.isEmpty()) {
			throw new IllegalArgumentException("value MUST NOT be null nor empty");
		}

		SnapshotStatus result = UNAVAILABLE;
		for (SnapshotStatus v : values()) {
			if (v.value.compareTo(value) == 0) {
				result = v;
				break;
			}
		}
		return result;
	}
}
