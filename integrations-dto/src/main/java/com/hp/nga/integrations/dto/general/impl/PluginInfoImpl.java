package com.hp.nga.integrations.dto.general.impl;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.hp.nga.integrations.dto.general.PluginInfo;

/**
 * Created by gullery on 03/01/2016.
 */

@JsonIgnoreProperties(ignoreUnknown = true)
class PluginInfoImpl implements PluginInfo {
	private String version;

	public PluginInfoImpl() {
	}

	public PluginInfoImpl(String version) {
		this.version = version;
	}

	public String getVersion() {
		return version;
	}

	public PluginInfo setVersion(String version) {
		this.version = version;
		return this;
	}
}
