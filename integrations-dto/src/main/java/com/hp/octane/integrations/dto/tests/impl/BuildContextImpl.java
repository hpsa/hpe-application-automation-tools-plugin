package com.hp.octane.integrations.dto.tests.impl;

import com.hp.octane.integrations.dto.tests.BuildContext;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Created by lev on 06/03/2016.
 */

@XmlRootElement(name = "build")
@XmlAccessorType(XmlAccessType.NONE)
public class BuildContextImpl implements BuildContext {

	@XmlAttribute(name = "server_id")
	private String serverId;

	@XmlAttribute(name = "job_id")
	private String jobId;

	@XmlAttribute(name = "job_name")
	private String jobName;

	@XmlAttribute(name = "build_id")
	private String buildId;

	@XmlAttribute(name = "build_name")
	private String buildName;

	@XmlAttribute(name = "sub_type")
	private String subType;

	public String getServerId() {
		return serverId;
	}

	public BuildContext setServerId(String serverId) {
		this.serverId = serverId;
		return this;
	}

	public String getJobId() {
		return jobId;
	}

	public BuildContext setJobId(String jobId) {
		this.jobId = jobId;
		return this;
	}

	public String getJobName() {
		return jobName;
	}

	public BuildContext setJobName(String jobName) {
		this.jobName = jobName;
		return this;
	}

	public String getBuildId() {
		return buildId;
	}

	public BuildContext setBuildId(String buildId) {
		this.buildId = buildId;
		return this;
	}

	public String getBuildName() {
		return buildName;
	}

	public BuildContext setBuildName(String buildName) {
		this.buildName = buildName;
		return this;
	}

	public String getSubType() {
		return subType;
	}

	public BuildContext setSubType(String subType) {
		this.subType = subType;
		return this;
	}
}
