package com.hp.octane.integrations.dto.configuration;

import com.hp.octane.integrations.dto.configuration.CIProxyConfiguration;

/**
 * Created by gullery on 11/02/2016.
 */

class CIProxyConfigurationImpl implements CIProxyConfiguration {
	private String host;
	private Integer port;
	private String username;
	private String password;

	public String getHost() {
		return host;
	}

	public CIProxyConfiguration setHost(String host) {
		this.host = host;
		return this;
	}

	public Integer getPort() {
		return port;
	}

	public CIProxyConfiguration setPort(Integer port) {
		this.port = port;
		return this;
	}

	public String getUsername() {
		return username;
	}

	public CIProxyConfiguration setUsername(String username) {
		this.username = username;
		return this;
	}

	public String getPassword() {
		return password;
	}

	public CIProxyConfiguration setPassword(String password) {
		this.password = password;
		return this;
	}

	@Override
	public String toString() {
		return "CIProxyConfigurationImpl { " +
				"host=" + host +
				", port=" + port +
				", username=" + username + " }";
	}
}
