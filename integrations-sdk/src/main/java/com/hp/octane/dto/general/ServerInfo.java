package com.hp.octane.dto.general;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Created by gullery on 03/01/2016.
 */

@JsonIgnoreProperties(ignoreUnknown = true)
public class ServerInfo {
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
		this.url = url;
		this.instanceId = instanceId;
		this.instanceIdFrom = instanceIdFrom;
	}

	public CIServerTypes getType() {
		return type;
	}

	public void setType(CIServerTypes type) {
		this.type = type;
	}

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		if (url != null && url.endsWith("/")) {
			this.url = url.substring(0, url.length() - 1);
		} else {
			this.url = url;
		}
	}

	public String getInstanceId() {
		return instanceId;
	}

	public void setInstanceId(String instanceId) {
		this.instanceId = instanceId;
	}

	public Long getInstanceIdFrom() {
		return instanceIdFrom;
	}

	public void setInstanceIdFrom(Long instanceIdFrom) {
		this.instanceIdFrom = instanceIdFrom;
	}

	public Long getSendingTime() {
		return sendingTime;
	}

	public void setSendingTime(Long sendingTime) {
		this.sendingTime = sendingTime;
	}
}
