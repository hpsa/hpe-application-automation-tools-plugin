package com.hp.nga.integrations.dto.general;

/**
 * Created by gullery on 03/01/2016.
 * <p>
 * Description of Plugin Status
 */

public interface AggregatedInfo {
	ServerInfo getServer();

	AggregatedInfo setServer(ServerInfo server);

	PluginInfo getPlugin();

	AggregatedInfo setPlugin(PluginInfo plugin);
}
