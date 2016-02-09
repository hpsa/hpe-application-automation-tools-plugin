package com.hp.nga.integrations.dto.general;

/**
 * Created by gullery on 03/01/2016.
 * <p>
 * Description of Plugin Status
 */

public interface IAggregatedInfo {
	ServerInfo getServer();

	IAggregatedInfo setServer(ServerInfo server);

	PluginInfo getPlugin();

	IAggregatedInfo setPlugin(PluginInfo plugin);
}
