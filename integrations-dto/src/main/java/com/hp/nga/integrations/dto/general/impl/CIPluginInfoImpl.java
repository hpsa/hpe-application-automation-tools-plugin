package com.hp.nga.integrations.dto.general.impl;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.hp.nga.integrations.dto.general.CIPluginInfo;

/**
 * Created by gullery on 03/01/2016.
 */

@JsonIgnoreProperties(ignoreUnknown = true)
class CIPluginInfoImpl implements CIPluginInfo {
	private String version;

	public CIPluginInfoImpl() {
	}

	public CIPluginInfoImpl(String version) {
		this.version = version;
	}

	public String getVersion() {
		return version;
	}

	public CIPluginInfo setVersion(String version) {
		this.version = version;
		return this;
	}
}
