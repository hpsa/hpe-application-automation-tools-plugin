package com.hp.octane.plugins.jenkins.configuration;

import org.apache.commons.httpclient.Cookie;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.auth.AuthPolicy;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.PutMethod;
import org.apache.commons.httpclient.methods.StringRequestEntity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by gullery on 15/02/2015.
 */
public class RestUtils {

	private static HashMap<String, Cookie> cookies = new HashMap<String, Cookie>();
	private static final String sessionXml = "<session-parameters><client-type>Octane Plugin (Jenkins)</client-type><time-out>6</time-out></session-parameters>";

	private static void storeCookies(Cookie[] cookiesArray) {
		for (Cookie cookie : cookiesArray) {
			cookies.put(cookie.getName(), cookie);
		}
	}

	private static void applyCookies(HttpClient client) {
		client.getState().clearCookies();
		for (Cookie cookie : cookies.values()) {
			client.getState().addCookie(cookie);
		}
	}

	private static void login(String url, String username, String password) throws Exception {
		int status;
		HttpClient httpClient = new HttpClient();
		List<String> authPrefs = new ArrayList<String>(2);
		authPrefs.add(AuthPolicy.DIGEST);
		authPrefs.add(AuthPolicy.BASIC);
		httpClient.getParams().setParameter(AuthPolicy.AUTH_SCHEME_PRIORITY, authPrefs);

		//  LWSSO login
		PostMethod post = new PostMethod(url + "/qcbin/authentication-point/alm-authenticate");
		post.setRequestEntity(new StringRequestEntity(
				"<alm-authentication><user>" + username + "</user><password>" + password + "</password></alm-authentication>",
				"application/xml",
				"UTF-8"));
		post.addRequestHeader("Content-type", "application/xml; charset=utf-8");

		status = httpClient.executeMethod(post);
		System.out.println("Initial: " + status);
		storeCookies(httpClient.getState().getCookies());

		//  QC Session
		post = new PostMethod(url + "/qcbin/rest/site-session");
		post.setRequestEntity(new StringRequestEntity(sessionXml, "application/xml", "UTF-8"));
		applyCookies(httpClient);

		status = httpClient.executeMethod(post);
		System.out.println("Session: " + status);
		storeCookies(httpClient.getState().getCookies());
	}

	public static int put(String url, String path, String username, String password, String body) throws Exception {
		int status;
		if (cookies.size() == 0) {
			login(url, username, password);
		}

		HttpClient httpClient = new HttpClient();
		applyCookies(httpClient);

		PutMethod putMethod = new PutMethod(url + path);
		putMethod.setRequestHeader("X-XSRF-TOKEN", cookies.get("XSRF-TOKEN").getValue());
		putMethod.setRequestEntity(new StringRequestEntity(body, "application/json", "UTF-8"));

		status = httpClient.executeMethod(putMethod);

		if (status == 401) {
			System.out.println("seems like login needed...");
			login(url, username, password);
			put(url, path, username, password, body);
		}
		return status;
	}
}
