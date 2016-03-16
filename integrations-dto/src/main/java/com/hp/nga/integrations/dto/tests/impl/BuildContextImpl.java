package com.hp.nga.integrations.dto.tests.impl;

import com.hp.nga.integrations.dto.tests.BuildContext;

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

	@XmlAttribute(name = "build_sid")
	private String buildId;

	@XmlAttribute(name = "sub_type")
	private String subType;

	@XmlAttribute(name = "build_type")
	private String buildType;

	@XmlAttribute(name = "server")
	private String server;

	public String getBuildId() {
		return buildId;
	}

	public BuildContext setBuildId(String buildId) {
		this.buildId = buildId;
		return this;
	}

	public String getSubType() {
		return subType;
	}

	public BuildContext setSubType(String subType) {
		this.subType = subType;
		return this;
	}

	public String getBuildType() {
		return buildType;
	}

	public BuildContext setBuildType(String buildType) {
		this.buildType = buildType;
		return this;
	}

	public String getServer() {
		return server;
	}

	public BuildContext setServer(String server) {
		this.server = server;
		return this;
	}
}
