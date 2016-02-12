package com.hp.nga.integrations.dto.general;

import com.hp.nga.integrations.dto.DTOBase;

/**
 * Created by gullery on 03/01/2016.
 * <p>
 * Description of Plugin Status
 */

public interface CIProviderSummaryInfo extends DTOBase {

	ServerInfo getServer();

	CIProviderSummaryInfo setServer(ServerInfo server);

	PluginInfo getPlugin();

	CIProviderSummaryInfo setPlugin(PluginInfo plugin);
}
