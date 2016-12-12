package com.hp.octane.integrations.dto.impl.general;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.hp.octane.integrations.dto.api.general.CIPluginInfo;

/**
 * Created by gullery on 03/01/2016.
 */

@JsonIgnoreProperties(ignoreUnknown = true)
class CIPluginInfoImpl implements CIPluginInfo {
	private String version;

	public String getVersion() {
		return version;
	}

	public CIPluginInfo setVersion(String version) {
		this.version = version;
		return this;
	}
}
