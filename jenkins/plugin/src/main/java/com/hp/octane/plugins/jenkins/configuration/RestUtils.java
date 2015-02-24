package com.hp.octane.plugins.jenkins.configuration;

import org.apache.commons.httpclient.Cookie;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.PutMethod;
import org.apache.commons.httpclient.methods.StringRequestEntity;

/**
 * Created by gullery on 15/02/2015.
 */
public class RestUtils {

	private static Cookie[] cookies;
	private static final String sessionXml = "<session-parameters><client-type>Octane Jenkins Plugin</client-type><time-out>6</time-out></session-parameters>";

	private static HttpClient login(String url, String username, String password) throws Exception {
		int status;
		HttpClient httpClient = new HttpClient();

		//  LWSSO login
		PostMethod post = new PostMethod(url + "/qcbin/authentication-point/alm-authenticate");
		post.setRequestEntity(new StringRequestEntity(
				"<alm-authentication><user>" + username + "</user><password>" + password + "</password></alm-authentication>",
				"application/xml",
				"UTF-8"));

		status = httpClient.executeMethod(post);
		System.out.println(status);
		cookies = httpClient.getState().getCookies();

		//  QC Session
		post = new PostMethod(url + "/qcbin/rest/site-session");
		post.setRequestEntity(new StringRequestEntity(sessionXml, "application/xml", "UTF-8"));
		httpClient.getState().addCookies(cookies);

		status = httpClient.executeMethod(post);
		cookies = httpClient.getState().getCookies();
		System.out.println(status);
		return httpClient;
	}

	public static int put(String url, String path, String username, String password, String body) throws Exception {
		int status;
		HttpClient httpClient;
		if (cookies == null) {
			httpClient = login(url, username, password);
		} else {
			httpClient = new HttpClient();
			httpClient.getState().addCookies(cookies);
		}

		PutMethod putMethod = new PutMethod(url + path);
		putMethod.setRequestEntity(new StringRequestEntity(body, "application/json", "UTF-8"));
		for (Cookie c : cookies) {
			if (c.getName().equals("XSRF-TOKEN")) {
				putMethod.setRequestHeader("X-XSRF-TOKEN", c.getValue());
			}
		}
		status = httpClient.executeMethod(putMethod);

		if (status == 401) {
			System.out.println("seems like login needed...");
			login(url, username, password);
			put(url, path, username, password, body);
		}
		return status;
	}
}
