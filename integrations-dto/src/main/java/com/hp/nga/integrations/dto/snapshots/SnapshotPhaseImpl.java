package com.hp.nga.integrations.dto.snapshots;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: gullery
 * Date: 08/01/15
 * Time: 23:15
 * To change this template use File | Settings | File Templates.
 */

class SnapshotPhaseImpl implements SnapshotPhase {
	private String name;
	private boolean blocking;
	private List<SnapshotItem> builds;

	public String getName() {
		return name;
	}

	public SnapshotPhase setName(String name) {
		this.name = name;
		return this;
	}

	public boolean isBlocking() {
		return blocking;
	}

	public SnapshotPhase setBlocking(boolean blocking) {
		this.blocking = blocking;
		return this;
	}

	public List<SnapshotItem> getBuilds() {
		return builds;
	}

	public SnapshotPhase setBuilds(List<SnapshotItem> builds) {
		this.builds = builds;
		return this;
	}
}