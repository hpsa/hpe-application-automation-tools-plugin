package com.hp.nga.dto.general;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Created by gullery on 03/01/2016.
 * <p/>
 * Description of Plugin Status
 */

@JsonIgnoreProperties(ignoreUnknown = true)
public class AggregatedStatusInfo {
	private ServerInfo server;
	private PluginInfo plugin;

	public ServerInfo getServer() {
		return server;
	}

	public void setServer(ServerInfo server) {
		this.server = server;
	}

	public PluginInfo getPlugin() {
		return plugin;
	}

	public void setPlugin(PluginInfo plugin) {
		this.plugin = plugin;
	}
}
