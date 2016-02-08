package com.hp.nga.integrations.dto.general;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Created by gullery on 03/01/2016.
 * <p/>
 * Description of Plugin Status
 */

@JsonIgnoreProperties(ignoreUnknown = true)
public class AggregatedInfo {
	private IServerInfo server;
	private IPluginInfo plugin;

	public IServerInfo getServer() {
		return server;
	}

	public void setServer(IServerInfo server) {
		this.server = server;
	}

	public IPluginInfo getPlugin() {
		return plugin;
	}

	public void setPlugin(IPluginInfo plugin) {
		this.plugin = plugin;
	}
}
