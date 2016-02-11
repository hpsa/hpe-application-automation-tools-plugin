package com.hp.nga.integrations.dto.general.impl;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.hp.nga.integrations.dto.general.AggregatedInfo;
import com.hp.nga.integrations.dto.general.PluginInfo;
import com.hp.nga.integrations.dto.general.ServerInfo;

/**
 * Created by gullery on 03/01/2016.
 * <p/>
 * Description of Plugin Status
 */

@JsonIgnoreProperties(ignoreUnknown = true)
class AggregatedInfoImpl implements AggregatedInfo {
	private ServerInfo server;
	private PluginInfo plugin;

	public ServerInfo getServer() {
		return server;
	}

	public AggregatedInfo setServer(ServerInfo server) {
		this.server = server;
		return this;
	}

	public PluginInfo getPlugin() {
		return plugin;
	}

	public AggregatedInfo setPlugin(PluginInfo plugin) {
		this.plugin = plugin;
		return this;
	}
}
