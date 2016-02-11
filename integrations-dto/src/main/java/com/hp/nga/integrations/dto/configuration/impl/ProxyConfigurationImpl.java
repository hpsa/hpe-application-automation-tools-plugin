package com.hp.nga.integrations.dto.configuration.impl;

import com.hp.nga.integrations.dto.configuration.ProxyConfiguration;

/**
 * Created by gullery on 11/02/2016.
 */

class ProxyConfigurationImpl implements ProxyConfiguration {
	private String url;
	private String username;
	private String password;

	public String getUrl() {
		return url;
	}

	public ProxyConfiguration setUrl(String url) {
		this.url = url;
		return this;
	}

	public String getUsername() {
		return username;
	}

	public ProxyConfiguration setUsername(String username) {
		this.username = username;
		return this;
	}

	public String getPassword() {
		return password;
	}

	public ProxyConfiguration setPassword(String password) {
		this.password = password;
		return this;
	}
}
