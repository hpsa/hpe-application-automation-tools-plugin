package com.hp.nga.integrations.dto.general.impl;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.hp.nga.integrations.dto.general.CIPluginSDKInfo;

/**
 * Created by gullery on 03/01/2016.
 */

@JsonIgnoreProperties(ignoreUnknown = true)
class CIPluginSDKInfoImpl implements CIPluginSDKInfo {
	private Integer apiVersion;
	private String sdkVersion;

	public Integer getApiVersion() {
		return apiVersion;
	}

	public CIPluginSDKInfo setApiVersion(Integer apiVersion) {
		this.apiVersion = apiVersion;
		return this;
	}

	public String getSdkVersion() {
		return sdkVersion;
	}

	public CIPluginSDKInfo setSdkVersion(String sdkVersion) {
		this.sdkVersion = sdkVersion;
		return this;
	}
}
