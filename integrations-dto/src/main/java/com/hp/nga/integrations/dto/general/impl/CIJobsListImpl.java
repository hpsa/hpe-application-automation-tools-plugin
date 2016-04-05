package com.hp.nga.integrations.dto.general.impl;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.hp.nga.integrations.dto.general.CIJobsList;
import com.hp.nga.integrations.dto.pipelines.PipelineNode;

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
