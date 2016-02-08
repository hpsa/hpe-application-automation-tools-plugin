package com.hp.nga.integrations.dto.general;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Created by gullery on 03/01/2016.
 */

@JsonIgnoreProperties(ignoreUnknown = true)
public class PluginInfo implements IPluginInfo {
	private String version;

	public PluginInfo() {
	}

	public PluginInfo(String version) {
		this.version = version;
	}

	public String getVersion() {
		return version;
	}

	public IPluginInfo setVersion(String version) {
		this.version = version;
		return this;
	}
}
