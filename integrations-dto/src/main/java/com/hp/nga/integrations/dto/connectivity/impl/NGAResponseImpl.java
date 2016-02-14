package com.hp.nga.integrations.dto.connectivity.impl;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.hp.nga.integrations.dto.connectivity.NGAResponse;

import java.util.Map;

/**
 * Created by gullery on 07/01/2016.
 */

@JsonIgnoreProperties(ignoreUnknown = true)
public class NGAResponseImpl implements NGAResponse {
	private int status;
	private Map<String, String> headers;
	private String body;

	public int getStatus() {
		return status;
	}

	public NGAResponse setStatus(int status) {
		this.status = status;
		return this;
	}

	public Map<String, String> getHeaders() {
		return headers;
	}

	public NGAResponse setHeaders(Map<String, String> headers) {
		this.headers = headers;
		return this;
	}

	public String getBody() {
		return body;
	}

	public NGAResponse setBody(String body) {
		this.body = body;
		return this;
	}
}
