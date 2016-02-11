package com.hp.nga.integrations.dto.general;

import com.hp.nga.integrations.dto.DTOBase;

/**
 * Created by gullery on 08/02/2016.
 */

public interface PluginInfo extends DTOBase {

	String getVersion();

	PluginInfo setVersion(String version);
}
