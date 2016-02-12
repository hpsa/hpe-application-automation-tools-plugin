package com.hp.nga.integrations.dto.general.impl;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.hp.nga.integrations.dto.general.CIProviderSummaryInfo;
import com.hp.nga.integrations.dto.general.PluginInfo;
import com.hp.nga.integrations.dto.general.ServerInfo;

/**
 * Created by gullery on 03/01/2016.
 * <p/>
 * Description of Plugin Status
 */

@JsonIgnoreProperties(ignoreUnknown = true)
class CIProviderSummaryInfoImpl implements CIProviderSummaryInfo {
	private ServerInfo server;
	private PluginInfo plugin;

	public ServerInfo getServer() {
		return server;
	}

	public CIProviderSummaryInfo setServer(ServerInfo server) {
		this.server = server;
		return this;
	}

	public PluginInfo getPlugin() {
		return plugin;
	}

	public CIProviderSummaryInfo setPlugin(PluginInfo plugin) {
		this.plugin = plugin;
		return this;
	}
}
