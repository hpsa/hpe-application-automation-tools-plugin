package com.hp.nga.integrations.dto.general;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Created by gullery on 03/01/2016.
 */

@JsonIgnoreProperties(ignoreUnknown = true)
public class ServerInfo implements IServerInfo {
	private CIServerTypes type;
	private String version;
	private String url;
	private String instanceId;
	private Long instanceIdFrom;
	private Long sendingTime = System.currentTimeMillis();

	public ServerInfo() {
	}

	public ServerInfo(CIServerTypes type, String version, String url, String instanceId, Long instanceIdFrom) {
		this.type = type;
		this.version = version;
		this.url = normalizeURL(url);
		this.instanceId = instanceId;
		this.instanceIdFrom = instanceIdFrom;
	}

	public CIServerTypes getType() {
		return type;
	}

	public IServerInfo setType(CIServerTypes type) {
		this.type = type;
		return this;
	}

	public String getVersion() {
		return version;
	}

	public IServerInfo setVersion(String version) {
		this.version = version;
		return this;
	}

	public String getUrl() {
		return url;
	}

	public IServerInfo setUrl(String url) {
		this.url = normalizeURL(url);
		return this;
	}

	public String getInstanceId() {
		return instanceId;
	}

	public IServerInfo setInstanceId(String instanceId) {
		this.instanceId = instanceId;
		return this;
	}

	public Long getInstanceIdFrom() {
		return instanceIdFrom;
	}

	public IServerInfo setInstanceIdFrom(Long instanceIdFrom) {
		this.instanceIdFrom = instanceIdFrom;
		return this;
	}

	public Long getSendingTime() {
		return sendingTime;
	}

	public IServerInfo setSendingTime(Long sendingTime) {
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
