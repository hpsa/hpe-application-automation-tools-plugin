package com.hp.nga.integrations.dto.general;

/**
 * Created by gullery on 03/01/2016.
 * <p>
 * Description of Plugin Status
 */

public interface IAggregatedInfo {
	IServerInfo getServer();

	IAggregatedInfo setServer(IServerInfo server);

	IPluginInfo getPlugin();

	IAggregatedInfo setPlugin(IPluginInfo plugin);
}
