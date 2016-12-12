package com.hp.octane.integrations.dto.general;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.hp.octane.integrations.dto.general.CIPluginSDKInfo;
import com.hp.octane.integrations.dto.general.CIProviderSummaryInfo;
import com.hp.octane.integrations.dto.general.CIPluginInfo;
import com.hp.octane.integrations.dto.general.CIServerInfo;

/**
 * Created by gullery on 03/01/2016.
 * <p>
 * Description of Plugin Status
 */

@JsonIgnoreProperties(ignoreUnknown = true)
class CIProviderSummaryInfoImpl implements CIProviderSummaryInfo {
	private CIServerInfo server;
	private CIPluginInfo plugin;
	private CIPluginSDKInfo sdk;

	public CIServerInfo getServer() {
		return server;
	}

	public CIProviderSummaryInfo setServer(CIServerInfo server) {
		this.server = server;
		return this;
	}

	public CIPluginInfo getPlugin() {
		return plugin;
	}

	public CIProviderSummaryInfo setPlugin(CIPluginInfo plugin) {
		this.plugin = plugin;
		return this;
	}

	public CIPluginSDKInfo getSdk() {
		return sdk;
	}

	public CIProviderSummaryInfo setSdk(CIPluginSDKInfo sdk) {
		this.sdk = sdk;
		return this;
	}
}
