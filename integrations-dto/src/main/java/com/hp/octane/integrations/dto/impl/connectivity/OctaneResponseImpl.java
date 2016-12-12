package com.hp.octane.integrations.dto.impl.connectivity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.hp.octane.integrations.dto.api.connectivity.OctaneResponse;

import java.util.Map;

/**
 * Created by gullery on 07/01/2016.
 */

@JsonIgnoreProperties(ignoreUnknown = true)
class OctaneResponseImpl implements OctaneResponse {
	private int status;
	private Map<String, String> headers;
	private String body;

	public int getStatus() {
		return status;
	}

	public OctaneResponse setStatus(int status) {
		this.status = status;
		return this;
	}

	public Map<String, String> getHeaders() {
		return headers;
	}

	public OctaneResponse setHeaders(Map<String, String> headers) {
		this.headers = headers;
		return this;
	}

	public String getBody() {
		return body;
	}

	public OctaneResponse setBody(String body) {
		this.body = body;
		return this;
	}
}
