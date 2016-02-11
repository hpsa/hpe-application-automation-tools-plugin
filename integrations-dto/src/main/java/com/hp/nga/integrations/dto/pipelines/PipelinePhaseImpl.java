package com.hp.nga.integrations.dto.pipelines;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: gullery
 * Date: 08/01/15
 * Time: 23:15
 * To change this template use File | Settings | File Templates.
 */

class PipelinePhaseImpl implements PipelinePhase {
	private String name;
	private boolean blocking;
	private List<PipelineNode> jobs;

	public String getName() {
		return name;
	}

	public PipelinePhase setName(String name) {
		this.name = name;
		return this;
	}

	public boolean isBlocking() {
		return blocking;
	}

	public PipelinePhase setBlocking(boolean blocking) {
		this.blocking = blocking;
		return this;
	}

	public List<PipelineNode> getJobs() {
		return jobs;
	}

	public PipelinePhase setJobs(List<PipelineNode> jobs) {
		this.jobs = jobs;
		return this;
	}
}