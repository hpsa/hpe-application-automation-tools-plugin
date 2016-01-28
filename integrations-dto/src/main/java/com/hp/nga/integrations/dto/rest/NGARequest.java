package com.hp.nga.integrations.dto.rest;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.Map;

/**
 * Created by gullery on 07/01/2016.
 */

@JsonIgnoreProperties(ignoreUnknown = true)
public class NGARequest {
	private String url;
	private String method;
	private Map<String, String> headers;
	private String body;

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public String getMethod() {
		return method;
	}

	public void setMethod(String method) {
		this.method = method;
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
