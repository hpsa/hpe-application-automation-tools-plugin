package com.hp.octane.plugins.jenkins.configuration;

import org.apache.commons.httpclient.Cookie;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.StringRequestEntity;

/**
 * Created by gullery on 15/02/2015.
 */
public class RestUtils {

	private static Cookie[] cookies;
	private static final String loginXmlA = "<alm-authentication><user>admin</user><password></password></alm-authentication>";
	private static final String sessionXml = "<session-parameters><client-type>HP ALM Web UI</client-type><time-out>6</time-out></session-parameters>";

	private static HttpClient login() {
		int status;
		HttpClient httpClient = new HttpClient();
		try {
			//  LWSSO login
			PostMethod post = new PostMethod("http://localhost:8080/qcbin/authentication-point/alm-authenticate");
			post.setRequestEntity(new StringRequestEntity(loginXmlA, "application/xml", "UTF-8"));

			status = httpClient.executeMethod(post);
			System.out.println(status);
			cookies = httpClient.getState().getCookies();

			//  QC Session
			post = new PostMethod("http://localhost:8080/qcbin/rest/site-session");
			post.setRequestEntity(new StringRequestEntity(sessionXml, "application/xml", "UTF-8"));
			httpClient.getState().addCookies(cookies);

			status = httpClient.executeMethod(post);
			cookies = httpClient.getState().getCookies();
			System.out.println(status);
			return httpClient;
		} catch (Exception e) {
			System.out.println(e.getMessage());
			return null;
		}
	}

	public static int post(String url, String body) {
		try {
			int status;
			HttpClient httpClient;
			if (cookies == null) {
				httpClient = login();
			} else {
				httpClient = new HttpClient();
				httpClient.getState().addCookies(cookies);
			}

			PostMethod post = new PostMethod(url);
			post.setRequestEntity(new StringRequestEntity(body, "application/json", "UTF-8"));
			for (Cookie c : cookies) {
				if (c.getName().equals("XSRF-TOKEN")) {
					post.setRequestHeader("X-XSRF-TOKEN", c.getValue());
				}
			}
			status = httpClient.executeMethod(post);

			if (status == 401) {
				System.out.println("seems like login needed...");
				login();
				post(url, body);
			}
			return status;
		} catch (Exception e) {
			e.printStackTrace();
			return 0;
		}
	}
}
