package com.hp.octane.integrations.dto.general.impl;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.hp.octane.integrations.dto.general.CIJobsList;
import com.hp.octane.integrations.dto.pipelines.PipelineNode;

/**
 * Created by gullery on 06/01/2016.
 */

@JsonIgnoreProperties(ignoreUnknown = true)
class CIJobsListImpl implements CIJobsList {
	private PipelineNode[] jobs;

	public PipelineNode[] getJobs() {
		return jobs;
	}

	public CIJobsList setJobs(PipelineNode[] jobs) {
		this.jobs = jobs;
		return this;
	}
}
