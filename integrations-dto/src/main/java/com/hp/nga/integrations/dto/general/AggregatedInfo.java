package com.hp.nga.integrations.dto.general;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Created by gullery on 03/01/2016.
 * <p/>
 * Description of Plugin Status
 */

@JsonIgnoreProperties(ignoreUnknown = true)
public class AggregatedInfo implements IAggregatedInfo{
	private IServerInfo server;
	private IPluginInfo plugin;

	public IServerInfo getServer() {
		return server;
	}

	public IAggregatedInfo setServer(IServerInfo server) {
		this.server = server;
		return this;
	}

	public IPluginInfo getPlugin() {
		return plugin;
	}

	public IAggregatedInfo setPlugin(IPluginInfo plugin) {
		this.plugin = plugin;
		return this;
	}
}
