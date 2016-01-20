package com.hp.nga.integrations.rest;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.Map;

/**
 * Created by gullery on 07/01/2016.
 */

@JsonIgnoreProperties(ignoreUnknown = true)
public class NGAResponse {
	private int status;
	private Map<String, String> headers;
	private String body;

	public NGAResponse() {
	}

	public NGAResponse(int status, String body, Map<String, String> headers) {
		this.status = status;
		this.headers = headers;
		this.body = body;
	}

	public int getStatus() {
		return status;
	}

	public void setStatus(int status) {
		this.status = status;
	}

	@JsonInclude(JsonInclude.Include.NON_NULL)
	public Map<String, String> getHeaders() {
		return headers;
	}

	public void setHeaders(Map<String, String> headers) {
		this.headers = headers;
	}

	@JsonInclude(JsonInclude.Include.NON_NULL)
	public String getBody() {
		return body;
	}

	public void setBody(String body) {
		this.body = body;
	}
}
