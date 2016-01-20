package com.hp.nga.integrations.rest;

import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;

import java.io.IOException;

/**
 * Created by gullery on 14/01/2016.
 * <p/>
 * REST Client wrapper
 */

public class NGARestClient {
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

	public void get(String url) {
		HttpGet get = new HttpGet(url);
		try {
			CloseableHttpResponse response = httpClient.execute(get);
			System.out.println(response.getStatusLine().getStatusCode());
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
}
