package com.hp.nga.integrations.dto.general;

import com.hp.nga.integrations.dto.DTOBase;

/**
 * Created by gullery on 03/01/2016.
 * <p>
 * Description of Plugin Status
 */

public interface AggregatedInfo extends DTOBase {

	ServerInfo getServer();

	AggregatedInfo setServer(ServerInfo server);

	PluginInfo getPlugin();

	AggregatedInfo setPlugin(PluginInfo plugin);
}
