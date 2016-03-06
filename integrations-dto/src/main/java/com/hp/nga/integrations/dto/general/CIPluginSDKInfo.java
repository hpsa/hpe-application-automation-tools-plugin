package com.hp.nga.integrations.dto.general;

import com.hp.nga.integrations.dto.DTOBase;

/**
 * Created by gullery on 08/02/2016.
 * <p>
 * Plugin's SDK info descriptor; this is to be used by SDK itself to provider it's own data
 */

public interface CIPluginSDKInfo extends DTOBase {

	Integer getApiVersion();

	CIPluginSDKInfo setApiVersion(Integer integer);

	String getSdkVersion();

	CIPluginSDKInfo setSdkVersion(String version);
}
