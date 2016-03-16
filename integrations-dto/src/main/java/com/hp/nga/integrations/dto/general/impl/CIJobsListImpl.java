package com.hp.nga.integrations.dto.general.impl;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.hp.nga.integrations.dto.general.CIJobMetadata;
import com.hp.nga.integrations.dto.general.CIJobsList;

/**
 * Created by gullery on 06/01/2016.
 */

@JsonIgnoreProperties(ignoreUnknown = true)
class CIJobsListImpl implements CIJobsList {
	private CIJobMetadata[] jobs = new CIJobMetadata[0];

	public void setJobs(CIJobMetadata[] jobs) {
		this.jobs = jobs == null ? new CIJobMetadata[0] : jobs.clone();
	}

	public CIJobMetadata[] getJobs() {
		return jobs.clone();
	}
}
