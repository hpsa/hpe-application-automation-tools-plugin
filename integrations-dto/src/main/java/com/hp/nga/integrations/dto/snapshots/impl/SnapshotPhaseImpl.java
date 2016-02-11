package com.hp.nga.integrations.dto.snapshots.impl;

import com.hp.nga.integrations.dto.snapshots.SnapshotNode;
import com.hp.nga.integrations.dto.snapshots.SnapshotPhase;

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
	private List<SnapshotNode> builds;

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

	public List<SnapshotNode> getBuilds() {
		return builds;
	}

	public SnapshotPhase setBuilds(List<SnapshotNode> builds) {
		this.builds = builds;
		return this;
	}
}