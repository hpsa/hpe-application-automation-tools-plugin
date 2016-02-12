package com.hp.nga.integrations.dto.connectivity.impl;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.hp.nga.integrations.dto.connectivity.NGAHttpMethod;
import com.hp.nga.integrations.dto.connectivity.NGATaskAbridged;

import java.util.Map;

/**
 * Created by gullery on 08/01/2016.
 */

@JsonIgnoreProperties(ignoreUnknown = true)
public class NGATaskAbridgedImpl implements NGATaskAbridged {
	private static final long serialVersionUID = 6526471255422717731L;

	private String id;
	private String serviceId;
	private String url;
	private NGAHttpMethod method;
	private Map<String, String> headers;
	private String body;

	public String getId() {
		return id;
	}

	public NGATaskAbridged setId(String id) {
		this.id = id;
		return this;
	}

	public String getServiceId() {
		return serviceId;
	}

	public NGATaskAbridged setServiceId(String serviceId) {
		this.serviceId = serviceId;
		return this;
	}

	public String getUrl() {
		return url;
	}

	public NGATaskAbridged setUrl(String url) {
		this.url = url;
		return this;
	}

	public NGAHttpMethod getMethod() {
		return method;
	}

	public NGATaskAbridged setMethod(NGAHttpMethod method) {
		this.method = method;
		return this;
	}

	public Map<String, String> getHeaders() {
		return headers;
	}

	public NGATaskAbridged setHeaders(Map<String, String> headers) {
		this.headers = headers;
		return this;
	}

	public String getBody() {
		return body;
	}

	public NGATaskAbridged setBody(String body) {
		this.body = body;
		return this;
	}
}
