package com.hp.nga.integrations.dto.general;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Created by gullery on 03/01/2016.
 * <p/>
 * Description of Plugin Status
 */

@JsonIgnoreProperties(ignoreUnknown = true)
public class AggregatedInfo implements IAggregatedInfo{
	private ServerInfo server;
	private PluginInfo plugin;

	public ServerInfo getServer() {
		return server;
	}

	public IAggregatedInfo setServer(ServerInfo server) {
		this.server = server;
		return this;
	}

	public PluginInfo getPlugin() {
		return plugin;
	}

	public IAggregatedInfo setPlugin(PluginInfo plugin) {
		this.plugin = plugin;
		return this;
	}
}
