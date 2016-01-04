package com.hp.octane.plugins.common.bridge;

import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.PutMethod;
import org.apache.commons.httpclient.methods.StringRequestEntity;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by gullery on 12/08/2015.
 * <p/>
 * REST utilities provider, dedicated for internal (loop back) call to Jenkins itself for the Abridged Connectivity Use Case
 * This facility is NOT meant to call Jenkins' own APIs, will not pass security barier, but for our own 'octane' APIs only
 */

public class LoopBackRestService {
	static LoopBackResponse loopBackGet(String url, Map<String, String> headers) throws RuntimeException {
		HttpClient client;
		GetMethod getMethod = null;
		try {
			client = new HttpClient();
			getMethod = new GetMethod(preProcessURL(url));
			if (headers != null)
				for (Map.Entry<String, String> entry : headers.entrySet())
					getMethod.setRequestHeader(entry.getKey(), entry.getValue());
			client.executeMethod(getMethod);
			return new LoopBackResponse(getMethod);
		} catch (Exception e) {
			throw new RuntimeException(e);
		} finally {
			if (getMethod != null) {
				getMethod.releaseConnection();
			}
		}
	}

	static LoopBackResponse loopBackPut(String url, Map<String, String> headers, String body) throws RuntimeException {
		HttpClient client;
		PutMethod putMethod = null;
		try {
			client = new HttpClient();
			putMethod = new PutMethod(preProcessURL(url));
			if (headers != null)
				for (Map.Entry<String, String> entry : headers.entrySet())
					putMethod.setRequestHeader(entry.getKey(), entry.getValue());
			putMethod.setRequestEntity(new StringRequestEntity(body, "application/json", "utf-8"));
			client.executeMethod(putMethod);
			return new LoopBackResponse(putMethod);
		} catch (Exception e) {
			throw new RuntimeException(e);
		} finally {
			if (putMethod != null) {
				putMethod.releaseConnection();
			}
		}
	}

	static LoopBackResponse loopBackPost(String url, Map<String, String> headers, String body) throws RuntimeException {
		HttpClient client;
		PostMethod postMethod = null;
		String contentType = null;
		try {
			client = new HttpClient();
			postMethod = new PostMethod(preProcessURL(url));
			if (headers != null) {
				for (Map.Entry<String, String> entry : headers.entrySet()) {
					postMethod.setRequestHeader(entry.getKey(), entry.getValue());
					if (entry.getKey().toLowerCase().equals("content-type")) {
						contentType = entry.getValue();
					}
				}
			}
			postMethod.setRequestEntity(
					new StringRequestEntity(
							body,
							contentType == null ? "application/x-www-form-urlencoded" : contentType,
							null)
			);
			client.executeMethod(postMethod);
			return new LoopBackResponse(postMethod);
		} catch (Exception e) {
			throw new RuntimeException(e);
		} finally {
			if (postMethod != null) {
				postMethod.releaseConnection();
			}
		}
	}

	static private String preProcessURL(String input) throws MalformedURLException, URISyntaxException {
		URL url = new URL(input);
		URI uri = new URI(url.getProtocol(), url.getUserInfo(), url.getHost(), url.getPort(), url.getPath(), url.getQuery(), url.getRef());
		return uri.toASCIIString();
	}

	static class LoopBackResponse {
		final int statusCode;
		final Map<String, String> headers;
		final String body;

		LoopBackResponse(int statusCode, Map<String, String> headers, String body) {
			this.statusCode = statusCode;
			this.headers = headers == null ? new HashMap<String, String>() : headers;
			this.body = body;
		}

		private LoopBackResponse(HttpMethod method) {
			String tmpBody;
			statusCode = method.getStatusCode();
			headers = new HashMap<String, String>();
			for (Header h : method.getResponseHeaders()) headers.put(h.getName(), h.getValue());

			try {
				tmpBody = method.getResponseBodyAsString();
			} catch (IOException ioe) {
				tmpBody = "{\"error\":\"The task was processed, but failed to retrieve the response body: " + ioe.getMessage() + "\"}";
			}
			body = tmpBody;
		}
	}
}
