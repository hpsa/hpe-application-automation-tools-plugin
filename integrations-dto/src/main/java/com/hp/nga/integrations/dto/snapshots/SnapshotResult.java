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

public enum SnapshotResult {
	UNAVAILABLE("unavailable"),
	UNSTABLE("unstable"),
	ABORTED("aborted"),
	FAILURE("failure"),
	SUCCESS("success");

	private String value;

	SnapshotResult(String value) {
		this.value = value;
	}

	@JsonValue
	public String value() {
		return value;
	}

	@JsonCreator
	public static SnapshotResult fromValue(String value) {
		if (value == null || value.isEmpty()) {
			throw new IllegalArgumentException("value MUST NOT be null nor empty");
		}

		SnapshotResult result = UNAVAILABLE;
		for (SnapshotResult v : values()) {
			if (v.value.compareTo(value) == 0) {
				result = v;
				break;
			}
		}
		return result;
	}
}
