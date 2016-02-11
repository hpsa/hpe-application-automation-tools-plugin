package com.hp.nga.integrations.dto.general.impl;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.hp.nga.integrations.dto.general.JobConfig;
import com.hp.nga.integrations.dto.general.JobsList;

/**
 * Created by gullery on 06/01/2016.
 */

@JsonIgnoreProperties(ignoreUnknown = true)
class JobsListImpl implements JobsList {
	private JobConfig[] jobs = new JobConfig[0];

	public void setJobs(JobConfig[] jobs) {
		this.jobs = jobs == null ? new JobConfig[0] : jobs.clone();
	}

	public JobConfig[] getJobs() {
		return jobs.clone();
	}
}
