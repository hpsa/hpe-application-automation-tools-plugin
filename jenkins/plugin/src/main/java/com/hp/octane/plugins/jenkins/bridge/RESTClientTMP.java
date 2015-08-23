package com.hp.octane.plugins.jenkins.bridge;

import net.sf.json.JSONObject;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PutMethod;
import org.apache.commons.httpclient.methods.StringRequestEntity;

import java.io.IOException;
import java.net.ConnectException;
import java.util.Map;

/**
 * Created by gullery on 12/08/2015.
 */

public class RESTClientTMP {
	static String get(String url, Map<String, String> headers) {
		HttpClient client = new HttpClient();
		GetMethod getMethod = new GetMethod(url);
		if (headers != null)
			for (Map.Entry<String, String> entry : headers.entrySet())
				getMethod.setRequestHeader(entry.getKey(), entry.getValue());
		try {
			client.executeMethod(getMethod);
			return getMethod.getResponseBodyAsString();
		} catch (ConnectException ce) {
			throw new TemporaryException("temporary fail", ce);
		} catch (IOException ioe) {
			throw new FatalException("fatal fail", ioe);
		} finally {
			getMethod.releaseConnection();
		}
	}

	static void put(String url, int status, Map<String, String> headers, String body) {
		HttpClient client = new HttpClient();
		PutMethod putMethod = new PutMethod(url);
		JSONObject json = new JSONObject();
		json.put("statusCode", status);
		if (headers != null) json.put("headers", "");
		json.put("body", body);
		try {
			putMethod.setRequestEntity(new StringRequestEntity(json.toString(), "application/json", "utf-8"));
			client.executeMethod(putMethod);
		} catch (Exception e) {
			throw new RuntimeException("request failed", e);
		} finally {
			putMethod.releaseConnection();
		}
	}

	static class FatalException extends RuntimeException {
		FatalException(String message, Throwable cause) {
			super(message, cause);
		}
	}

	static class TemporaryException extends RuntimeException {
		TemporaryException(String message, Throwable cause) {
			super(message, cause);
		}
	}
}
