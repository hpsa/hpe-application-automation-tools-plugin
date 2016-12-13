package com.hp.octane.integrations.dto.general;

import com.hp.octane.integrations.dto.DTOBase;

/**
 * Created by gullery on 08/02/2016.
 * <p>
 * Plugin info descriptor
 */

public interface CIPluginInfo extends DTOBase {

	String getVersion();

	CIPluginInfo setVersion(String version);
}
