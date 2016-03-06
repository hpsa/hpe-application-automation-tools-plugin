package com.hp.nga.integrations.dto.general.impl;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.hp.nga.integrations.dto.general.CIServerTypes;
import com.hp.nga.integrations.dto.general.CIServerInfo;

/**
 * Created by gullery on 03/01/2016.
 */

@JsonIgnoreProperties(ignoreUnknown = true)
class CIServerInfoImpl implements CIServerInfo {
	private CIServerTypes type;
	private String version;
	private String url;
	private String instanceId;
	private Long instanceIdFrom;
	private Long sendingTime = System.currentTimeMillis();

	public CIServerInfoImpl() {
	}

	public CIServerInfoImpl(CIServerTypes type, String version, String url, String instanceId, Long instanceIdFrom) {
		this.type = type;
		this.version = version;
		this.url = normalizeURL(url);
		this.instanceId = instanceId;
		this.instanceIdFrom = instanceIdFrom;
	}

	public CIServerTypes getType() {
		return type;
	}

	public CIServerInfo setType(CIServerTypes type) {
		this.type = type;
		return this;
	}

	public String getVersion() {
		return version;
	}

	public CIServerInfo setVersion(String version) {
		this.version = version;
		return this;
	}

	public String getUrl() {
		return url;
	}

	public CIServerInfo setUrl(String url) {
		this.url = normalizeURL(url);
		return this;
	}

	public String getInstanceId() {
		return instanceId;
	}

	public CIServerInfo setInstanceId(String instanceId) {
		this.instanceId = instanceId;
		return this;
	}

	public Long getInstanceIdFrom() {
		return instanceIdFrom;
	}

	public CIServerInfo setInstanceIdFrom(Long instanceIdFrom) {
		this.instanceIdFrom = instanceIdFrom;
		return this;
	}

	public Long getSendingTime() {
		return sendingTime;
	}

	public CIServerInfo setSendingTime(Long sendingTime) {
		this.sendingTime = sendingTime;
		return this;
	}

	private String normalizeURL(String input) {
		String result;
		if (input != null && input.endsWith("/")) {
			result = input.substring(0, input.length() - 1);
		} else {
			result = input;
		}
		return result;
	}
}
