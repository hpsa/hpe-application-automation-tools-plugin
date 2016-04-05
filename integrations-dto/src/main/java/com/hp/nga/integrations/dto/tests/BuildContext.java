package com.hp.nga.integrations.dto.tests;

import com.hp.nga.integrations.dto.DTOBase;

/**
 * Created by gullery on 06/03/2016.
 */

public interface BuildContext extends DTOBase {

	String getServerId();

	BuildContext setServerId(String serverId);

	String getJobId();

	BuildContext setJobId(String jobId);

	String getJobName();

	BuildContext setJobName(String jobName);

	String getBuildId();

	BuildContext setBuildId(String buildId);

	String getBuildName();

	BuildContext setBuildName(String buildName);

	String getSubType();

	BuildContext setSubType(String subType);
}
