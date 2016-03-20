package com.hp.nga.integrations.dto.tests;

import com.hp.nga.integrations.dto.DTOBase;

/**
 * Created by gullery on 06/03/2016.
 */

public interface BuildContext extends DTOBase {

	Long getBuildId();

	BuildContext setBuildId(Long buildId);

	String getSubType();

	BuildContext setSubType(String subType);

	String getBuildType();

	BuildContext setBuildType(String buildType);

	String getServer();

	BuildContext setServer(String server);
}
