package com.hp.nga.integrations.dto.snapshots;

import com.hp.nga.integrations.dto.pipelines.StructureItem;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: gullery
 * Date: 08/01/15
 * Time: 23:15
 * To change this template use File | Settings | File Templates.
 */

public final class SnapshotPhase {
	private String name;
	private boolean blocking;
	private List<StructureItem> builds;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public boolean isBlocking() {
		return blocking;
	}

	public void setBlocking(boolean blocking) {
		this.blocking = blocking;
	}

	public List<StructureItem> getBuilds() {
		return builds;
	}

	public void setBuilds(List<StructureItem> builds) {
		this.builds = builds;
	}
}