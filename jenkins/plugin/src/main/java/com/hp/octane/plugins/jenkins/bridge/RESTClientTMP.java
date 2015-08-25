package com.hp.octane.plugins.jenkins.bridge;

import net.sf.json.JSONObject;
import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.PutMethod;
import org.apache.commons.httpclient.methods.StringRequestEntity;

import java.io.IOException;
import java.net.ConnectException;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by gullery on 12/08/2015.
 * <p/>
 * This class and all it's content to be removed once MQMRestClient migration finalized
 */

public class RESTClientTMP {
	static String getTask(String url, Map<String, String> headers) {
		HttpClient client = new HttpClient();
		GetMethod getMethod = new GetMethod(url);
		if (headers != null)
			for (Map.Entry<String, String> entry : headers.entrySet())
				getMethod.setRequestHeader(entry.getKey(), entry.getValue());
		try {
			client.executeMethod(getMethod);
			if (getMethod.getStatusCode() == 404) {
				//  add more like this; this is, again, temporary handling to be replaced with MQMRestClient
				throw new FatalException("fatal fail; MQM server is not compatible with this feature; responded with 404", null);
			}
			return getMethod.getResponseBodyAsString();
		} catch (ConnectException ce) {
			throw new TemporaryException("temporary fail", ce);
		} catch (IOException ioe) {
			throw new FatalException("fatal fail", ioe);
		} finally {
			getMethod.releaseConnection();
		}
	}

	static void putTaskResult(String url, LoopbackResponse response) {
		HttpClient client = new HttpClient();
		PutMethod putMethod = new PutMethod(url);
		JSONObject json = new JSONObject();
		json.put("statusCode", response.statusCode);
		json.put("headers", response.headers);          //  TODO: verify this map approach
		json.put("body", response.body);
		try {
			putMethod.setRequestEntity(new StringRequestEntity(json.toString(), "application/json", "utf-8"));
			client.executeMethod(putMethod);
		} catch (Exception e) {
			throw new RuntimeException("request failed", e);
		} finally {
			putMethod.releaseConnection();
		}
	}

	static LoopbackResponse loopbackGet(String url, Map<String, String> headers) {
		HttpClient client = new HttpClient();
		GetMethod getMethod = new GetMethod(url);
		try {
			if (headers != null)
				for (Map.Entry<String, String> entry : headers.entrySet())
					getMethod.setRequestHeader(entry.getKey(), entry.getValue());
			client.executeMethod(getMethod);
			return new LoopbackResponse(getMethod);
		} catch (ConnectException ce) {
			throw new TemporaryException("temporary fail", ce);
		} catch (IOException ioe) {
			throw new FatalException("fatal fail", ioe);
		} finally {
			getMethod.releaseConnection();
		}
	}

	static LoopbackResponse loopbackPut(String url, Map<String, String> headers, String body) {
		HttpClient client = new HttpClient();
		PutMethod putMethod = new PutMethod(url);
		try {
			if (headers != null)
				for (Map.Entry<String, String> entry : headers.entrySet())
					putMethod.setRequestHeader(entry.getKey(), entry.getValue());
			putMethod.setRequestEntity(new StringRequestEntity(body, "application/json", "utf-8"));
			client.executeMethod(putMethod);
			return new LoopbackResponse(putMethod);
		} catch (ConnectException ce) {
			throw new TemporaryException("temporary fail", ce);
		} catch (IOException ioe) {
			throw new FatalException("fatal fail", ioe);
		} finally {
			putMethod.releaseConnection();
		}
	}

	static LoopbackResponse loopbackPost(String url, Map<String, String> headers, String body) {
		HttpClient client = new HttpClient();
		PostMethod postMethod = new PostMethod(url);
		try {
			if (headers != null)
				for (Map.Entry<String, String> entry : headers.entrySet())
					postMethod.setRequestHeader(entry.getKey(), entry.getValue());
			postMethod.setRequestEntity(new StringRequestEntity(body, "application/json", "utf-8"));
			client.executeMethod(postMethod);
			return new LoopbackResponse(postMethod);
		} catch (ConnectException ce) {
			throw new TemporaryException("temporary fail", ce);
		} catch (IOException ioe) {
			throw new FatalException("fatal fail", ioe);
		} finally {
			postMethod.releaseConnection();
		}
	}

	static class LoopbackResponse {
		int statusCode;
		Map<String, String> headers = new HashMap<String, String>();
		String body;

		private LoopbackResponse(HttpMethod method) {
			statusCode = method.getStatusCode();
			for (Header h : method.getResponseHeaders())
				headers.put(h.getName(), h.getValue());
			try {
				body = method.getResponseBodyAsString();
			} catch (IOException ioe) {
				body = "{\"error\":\"" + ioe.getMessage() + "\"}";
			}
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
