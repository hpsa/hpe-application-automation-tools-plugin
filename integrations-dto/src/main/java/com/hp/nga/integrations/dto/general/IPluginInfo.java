package com.hp.nga.integrations.dto.general;

/**
 * Created by gullery on 08/02/2016.
 */

public interface IPluginInfo {
	Class<PluginInfo> CONCRETE = PluginInfo.class;

	String getVersion();

	IPluginInfo setVersion(String version);
}
