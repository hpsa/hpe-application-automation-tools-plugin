package com.hp.nga.integrations.services.rest;

import com.hp.nga.integrations.dto.connectivity.NGARequest;
import com.hp.nga.integrations.dto.connectivity.NGAResponse;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;

import java.io.IOException;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Created by gullery on 14/01/2016.
 * <p>
 * REST Client default implementation
 */

class NGARestClientImpl implements NGARestClient {
	private final static Set<Integer> AUTHENTICATION_ERROR_CODES;
	private final CloseableHttpClient httpClient;

	static {
		AUTHENTICATION_ERROR_CODES = new HashSet<Integer>();
		AUTHENTICATION_ERROR_CODES.add(401);
	}

	NGARestClientImpl() {
		PoolingHttpClientConnectionManager connectionManager = new PoolingHttpClientConnectionManager();
		httpClient = HttpClients.custom()
				.setConnectionManager(connectionManager)
				.build();
	}

	public NGAResponse execute(NGARequest request) {
		return null;
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
