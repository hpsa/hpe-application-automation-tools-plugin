package com.hp.nga.integrations.dto.pipelines;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: gullery
 * Date: 08/01/15
 * Time: 23:15
 * To change this template use File | Settings | File Templates.
 */

public final class StructurePhaseImpl implements StructurePhase {
	private String name;
	private boolean blocking;
	private List<StructureItem> jobs;

	public String getName() {
		return name;
	}

	public StructurePhase setName(String name) {
		this.name = name;
		return this;
	}

	public boolean isBlocking() {
		return blocking;
	}

	public StructurePhase setBlocking(boolean blocking) {
		this.blocking = blocking;
		return this;
	}

	public List<StructureItem> getJobs() {
		return jobs;
	}

	public StructurePhase setJobs(List<StructureItem> jobs) {
		this.jobs = jobs;
		return this;
	}
}