package com.hp.octane.plugins.jenkins.bridge;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.methods.GetMethod;

import java.util.Map;

/**
 * Created by gullery on 12/08/2015.
 */

public class RESTClientTMP {
	private static final HttpClient client = new HttpClient();

	static String get(String url, Map<String, String> headers) {
		HttpMethod getMethod = new GetMethod(url);
		try {
			client.executeMethod(getMethod);
			return getMethod.getResponseBodyAsString();
		} catch (Exception e) {
			throw new RuntimeException("request failed", e);
		}
	}
}
