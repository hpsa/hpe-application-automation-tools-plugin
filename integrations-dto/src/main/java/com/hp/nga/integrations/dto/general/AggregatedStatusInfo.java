package com.hp.nga.integrations.dto.general;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Created by gullery on 03/01/2016.
 * <p/>
 * Description of Plugin Status
 */

@JsonIgnoreProperties(ignoreUnknown = true)
public class AggregatedStatusInfo {
	private ServerInfoDTO server;
	private PluginInfoDTO plugin;

	public ServerInfoDTO getServer() {
		return server;
	}

	public void setServer(ServerInfoDTO server) {
		this.server = server;
	}

	public PluginInfoDTO getPlugin() {
		return plugin;
	}

	public void setPlugin(PluginInfoDTO plugin) {
		this.plugin = plugin;
	}
}
