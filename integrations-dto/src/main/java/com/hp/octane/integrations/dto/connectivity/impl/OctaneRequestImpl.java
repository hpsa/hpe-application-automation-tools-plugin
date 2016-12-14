package com.hp.octane.integrations.dto.connectivity.impl;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.hp.octane.integrations.dto.connectivity.OctaneRequest;
import com.hp.octane.integrations.dto.connectivity.HttpMethod;

import java.util.Map;

/**
 * Created by gullery on 07/01/2016.
 */

@JsonIgnoreProperties(ignoreUnknown = true)
class OctaneRequestImpl implements OctaneRequest {
	private String url;
	private HttpMethod method;
	private Map<String, String> headers;
	private String body;

	public String getUrl() {
		return url;
	}

	public OctaneRequest setUrl(String url) {
		this.url = url;
		return this;
	}

	public HttpMethod getMethod() {
		return method;
	}

	public OctaneRequest setMethod(HttpMethod method) {
		this.method = method;
		return this;
	}

	public Map<String, String> getHeaders() {
		return headers;
	}

	public OctaneRequest setHeaders(Map<String, String> headers) {
		this.headers = headers;
		return this;
	}

	public String getBody() {
		return body;
	}

	public OctaneRequest setBody(String body) {
		this.body = body;
		return this;
	}
}
