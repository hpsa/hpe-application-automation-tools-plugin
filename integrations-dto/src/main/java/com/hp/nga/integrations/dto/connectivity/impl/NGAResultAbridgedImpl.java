package com.hp.nga.integrations.dto.connectivity.impl;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.hp.nga.integrations.dto.connectivity.NGAResultAbridged;

import java.util.Map;

/**
 * Created by gullery on 08/01/2016.
 */

@JsonIgnoreProperties(ignoreUnknown = true)
class NGAResultAbridgedImpl implements NGAResultAbridged {
	private String id;
	private String serviceId;
	private int status;
	private Map<String, String> headers;
	private String body;

	public String getId() {
		return id;
	}

	public NGAResultAbridged setId(String id) {
		this.id = id;
		return this;
	}

	public String getServiceId() {
		return serviceId;
	}

	public NGAResultAbridged setServiceId(String serviceId) {
		this.serviceId = serviceId;
		return this;
	}

	public int getStatus() {
		return status;
	}

	public NGAResultAbridged setStatus(int status) {
		this.status = status;
		return this;
	}

	public Map<String, String> getHeaders() {
		return headers;
	}

	public NGAResultAbridged setHeaders(Map<String, String> headers) {
		this.headers = headers;
		return this;
	}

	public String getBody() {
		return body;
	}

	public NGAResultAbridged setBody(String body) {
		this.body = body;
		return this;
	}
}
