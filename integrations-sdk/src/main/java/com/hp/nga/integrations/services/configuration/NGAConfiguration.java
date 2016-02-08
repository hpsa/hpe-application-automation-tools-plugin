package com.hp.nga.integrations.services.configuration;

import java.net.MalformedURLException;
import java.net.URL;

/**
 * Created by gullery on 08/01/2016.
 *
 * NGA Server configuration descriptor
 */

public class NGAConfiguration {

	private String url;
	private Long sharedSpace;
	private String username;
	private String password;

	public NGAConfiguration() {
	}

	public NGAConfiguration(String url, Long sharedSpace, String username, String password) {
		this.url = url;
		this.sharedSpace = sharedSpace;
		this.username = username;
		this.password = password;
	}

	public NGAConfiguration(NGAConfiguration config) {
		this.url = config.url;
		this.sharedSpace = config.sharedSpace;
		this.username = config.username;
		this.password = config.password;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public Long getSharedSpace() {
		return sharedSpace;
	}

	public void setSharedSpace(Long sharedSpace) {
		this.sharedSpace = sharedSpace;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public boolean isValid() {
		boolean result = false;
		if (url != null && !url.isEmpty() && sharedSpace != null) {
			try {
				URL tmp = new URL(url);
				result = true;
			} catch (MalformedURLException mue) {
				result = false;
			}
		}
		return result;
	}
}
