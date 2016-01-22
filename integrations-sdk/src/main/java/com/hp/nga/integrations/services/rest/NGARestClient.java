package com.hp.nga.integrations.services.rest;

import com.hp.nga.integrations.services.serialization.SerializationService;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.CloseableHttpClient;

import java.io.IOException;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Created by gullery on 14/01/2016.
 * <p>
 * REST Client wrapper
 */

public class NGARestClient {
	private final static Set<Integer> AUTHENTICATION_ERROR_CODES;

	static {
		AUTHENTICATION_ERROR_CODES = new HashSet<Integer>();
		AUTHENTICATION_ERROR_CODES.add(401);
	}

	private final CloseableHttpClient httpClient;

	NGARestClient(CloseableHttpClient httpClient) {
		if (httpClient == null) {
			throw new IllegalArgumentException("HTTP Client MUST NOT be null");
		}

		this.httpClient = httpClient;
	}

	public void post() {
		throw new RuntimeException("not implemented");
	}

	public <T> T get(String url, Map<String, String> headers, Class<T> targetClass) {
		HttpGet get = new HttpGet(url);
		setOutgoingHeaders(get, headers);
		try {
			CloseableHttpResponse response = httpClient.execute(get);
			handleResponse(response, get);
			return SerializationService.fromJSON(new BasicResponseHandler().handleResponse(response), targetClass);
		} catch (IOException ioe) {
			ioe.printStackTrace();
			throw new RuntimeException("failed to GET " + url, ioe);
		}
	}

	public void put() {
		throw new RuntimeException("not implemented");
	}

	public void delete() {
		throw new RuntimeException("not implemented");
	}

	private void setOutgoingHeaders(HttpRequestBase requestBase, Map<String, String> headers) {
		if (headers != null && !headers.isEmpty()) {
			for (Map.Entry<String, String> e : headers.entrySet()) {
				requestBase.setHeader(e.getKey(), e.getValue());
			}
		}
	}

	private void handleResponse(HttpResponse response, HttpUriRequest request) {
		if (AUTHENTICATION_ERROR_CODES.contains(response.getStatusLine().getStatusCode())) {
			if (login()) {
				try {
					httpClient.execute(request);
				} catch (IOException ioe) {
					ioe.printStackTrace();
				}
			} else {
				System.out.println("abandon");
			}
		} else {
			System.out.println(response.getStatusLine().getStatusCode());
		}
	}

	private boolean login() {
		return true;
	}
}
