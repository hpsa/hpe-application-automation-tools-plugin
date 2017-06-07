// (C) Copyright 2003-2015 Hewlett-Packard Development Company, L.P.

package com.hpe.application.automation.tools.octane.tests.build;

public final class BuildDescriptor {

	private final String jobId;
	private final String jobName;
	private final String buildId;
	private final String buildName;
	private final String subType;

	public BuildDescriptor(String jobId, String jobName, String buildId, String buildName, String subType) {
		this.jobId = jobId;
		this.jobName = jobName;
		this.buildId = buildId;
		this.buildName = buildName;
		this.subType = subType;
	}

	public String getJobId() {
		return jobId;
	}

	public String getJobName() {
		return jobName;
	}

	public String getBuildId() {
		return buildId;
	}

	public String getBuildName() {
		return buildName;
	}

	public String getSubType() {
		return subType;
	}
}
