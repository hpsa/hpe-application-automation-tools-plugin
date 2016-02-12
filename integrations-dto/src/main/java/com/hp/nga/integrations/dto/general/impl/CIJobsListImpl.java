package com.hp.nga.integrations.dto.general.impl;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.hp.nga.integrations.dto.general.CIJobConfig;
import com.hp.nga.integrations.dto.general.CIJobsList;

/**
 * Created by gullery on 06/01/2016.
 */

@JsonIgnoreProperties(ignoreUnknown = true)
class CIJobsListImpl implements CIJobsList {
	private CIJobConfig[] jobs = new CIJobConfig[0];

	public void setJobs(CIJobConfig[] jobs) {
		this.jobs = jobs == null ? new CIJobConfig[0] : jobs.clone();
	}

	public CIJobConfig[] getJobs() {
		return jobs.clone();
	}
}
