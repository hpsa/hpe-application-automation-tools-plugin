package com.hp.octane.integrations.dto.connectivity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.hp.octane.integrations.dto.connectivity.HttpMethod;
import com.hp.octane.integrations.dto.connectivity.OctaneTaskAbridged;

import java.util.Map;

/**
 * Created by gullery on 08/01/2016.
 */

@JsonIgnoreProperties(ignoreUnknown = true)
class OctaneTaskAbridgedImpl implements OctaneTaskAbridged {
	private String id;
	private String serviceId;
	private String url;
	private HttpMethod method;
	private Map<String, String> headers;
	private String body;

	public String getId() {
		return id;
	}

	public OctaneTaskAbridged setId(String id) {
		this.id = id;
		return this;
	}

	public String getServiceId() {
		return serviceId;
	}

	public OctaneTaskAbridged setServiceId(String serviceId) {
		this.serviceId = serviceId;
		return this;
	}

	public String getUrl() {
		return url;
	}

	public OctaneTaskAbridged setUrl(String url) {
		this.url = url;
		return this;
	}

	public HttpMethod getMethod() {
		return method;
	}

	public OctaneTaskAbridged setMethod(HttpMethod method) {
		this.method = method;
		return this;
	}

	public Map<String, String> getHeaders() {
		return headers;
	}

	public OctaneTaskAbridged setHeaders(Map<String, String> headers) {
		this.headers = headers;
		return this;
	}

	public String getBody() {
		return body;
	}

	public OctaneTaskAbridged setBody(String body) {
		this.body = body;
		return this;
	}
}
