package com.hp.nga.integrations.dto.connectivity.impl;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.hp.nga.integrations.dto.connectivity.NGARequest;
import com.hp.nga.integrations.dto.connectivity.NGAHttpMethod;

import java.util.Map;

/**
 * Created by gullery on 07/01/2016.
 */

@JsonIgnoreProperties(ignoreUnknown = true)
class NGARequestImpl implements NGARequest {
	private String url;
	private NGAHttpMethod method;
	private Map<String, String> headers;
	private String body;

	public String getUrl() {
		return url;
	}

	public NGARequest setUrl(String url) {
		this.url = url;
		return this;
	}

	public NGAHttpMethod getMethod() {
		return method;
	}

	public NGARequest setMethod(NGAHttpMethod method) {
		this.method = method;
		return this;
	}

	public Map<String, String> getHeaders() {
		return headers;
	}

	public NGARequest setHeaders(Map<String, String> headers) {
		this.headers = headers;
		return this;
	}

	public String getBody() {
		return body;
	}

	public NGARequest setBody(String body) {
		this.body = body;
		return this;
	}
}
