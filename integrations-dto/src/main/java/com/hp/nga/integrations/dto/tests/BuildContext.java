package com.hp.nga.integrations.dto.tests;

import com.hp.nga.integrations.dto.DTOBase;

/**
 * Created by gullery on 06/03/2016.
 */

public interface BuildContext  extends DTOBase {

	long getBuildId();

	BuildContext setBuildId(long buildId);

	String getSubType();

	BuildContext setSubType(String subType);

	String getBuildType();

	BuildContext setBuildType(String buildType);

	String getServer();

	BuildContext setServer(String server);

}
