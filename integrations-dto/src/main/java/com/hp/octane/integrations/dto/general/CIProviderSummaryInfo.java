package com.hp.octane.integrations.dto.general;

import com.hp.octane.integrations.dto.DTOBase;

/**
 * Created by gullery on 03/01/2016.
 * <p>
 * CI Provider summary data container descriptor
 */

public interface CIProviderSummaryInfo extends DTOBase {

	CIServerInfo getServer();

	CIProviderSummaryInfo setServer(CIServerInfo server);

	CIPluginInfo getPlugin();

	CIProviderSummaryInfo setPlugin(CIPluginInfo plugin);

	CIPluginSDKInfo getSdk();

	CIProviderSummaryInfo setSdk(CIPluginSDKInfo sdk);
}
