/*
 *     Copyright 2017 Hewlett-Packard Development Company, L.P.
 *     Licensed under the Apache License, Version 2.0 (the "License");
 *     you may not use this file except in compliance with the License.
 *     You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS,
 *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *     See the License for the specific language governing permissions and
 *     limitations under the License.
 *
 */

package com.hpe.application.automation.tools.octane.client;

import com.hp.mqm.client.MqmConnectionConfig;
import com.hp.mqm.client.MqmRestClient;
import com.hp.mqm.client.MqmRestClientImpl;
import com.hp.mqm.client.UsernamePasswordProxyCredentials;
import hudson.Extension;
import hudson.ProxyConfiguration;
import hudson.util.Secret;
import jenkins.model.Jenkins;
import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Extension
public class JenkinsMqmRestClientFactoryImpl implements JenkinsMqmRestClientFactory {
	private static final Logger logger = LogManager.getLogger(JenkinsMqmRestClientFactoryImpl.class);
	private static final String CLIENT_TYPE = "HPE_CI_CLIENT";
	private MqmRestClient mqmRestClient;

	@Override
	public synchronized MqmRestClient obtain(String location, String sharedSpace, String username, Secret password) {
		if (mqmRestClient == null) {
			mqmRestClient = create(location, sharedSpace, username, password);
			logger.info("NGA REST Clint: initialized for " + location + " - " + sharedSpace + " - " + username);
		}
		return mqmRestClient;
	}

	@Override
	public MqmRestClient obtainTemp(String location, String sharedSpace, String username, Secret password) {
		MqmRestClient mqmRestClientTemp = create(location, sharedSpace, username, password);
		logger.info("NGA REST Clint: initialized for " + location + " - " + sharedSpace + " - " + username);
		return mqmRestClientTemp;
	}

	@Override
	public synchronized void updateMqmRestClient(String location, String sharedSpace, String username, Secret password) {
		mqmRestClient = create(location, sharedSpace, username, password);
		logger.info("NGA REST Clint: updated to " + location + " - " + sharedSpace + " - " + username);
	}

	private MqmRestClient create(String location, String sharedSpace, String username, Secret password) {
		MqmConnectionConfig clientConfig = new MqmConnectionConfig(location, sharedSpace, username, password.getPlainText(), CLIENT_TYPE);
		URL locationUrl;
		try {
			locationUrl = new URL(clientConfig.getLocation());
		} catch (MalformedURLException e) {
			throw new IllegalArgumentException(e);
		}
		if (isProxyNeeded(locationUrl.getHost())) {
			clientConfig.setProxyHost(getProxyHost());
			clientConfig.setProxyPort(getProxyPort());
			final String proxyUsername = getUsername();
			if (!StringUtils.isEmpty(proxyUsername)) {
				clientConfig.setProxyCredentials(new UsernamePasswordProxyCredentials(username, getPassword()));
			}
		}
		return new MqmRestClientImpl(clientConfig);
	}

	private String getProxyHost() {
		final ProxyConfiguration proxyConfiguration = Jenkins.getInstance().proxy;
		if (proxyConfiguration != null) {
			return proxyConfiguration.name;
		}
		return null;
	}

	private Integer getProxyPort() {
		final ProxyConfiguration proxyConfiguration = Jenkins.getInstance().proxy;
		if (proxyConfiguration != null) {
			return proxyConfiguration.port;
		}
		return null;
	}

	private String getUsername() {
		final ProxyConfiguration proxyConfiguration = Jenkins.getInstance().proxy;
		if (proxyConfiguration != null) {
			return proxyConfiguration.getUserName();
		}
		return null;
	}

	private String getPassword() {
		final ProxyConfiguration proxyConfiguration = Jenkins.getInstance().proxy;
		if (proxyConfiguration != null) {
			return proxyConfiguration.getPassword();
		}
		return null;
	}

	private boolean isProxyNeeded(final String host) {
		final ProxyConfiguration proxyConfiguration = Jenkins.getInstance().proxy;
		if (proxyConfiguration != null && !StringUtils.isEmpty(proxyConfiguration.name)) {
			// if any patterns match the host, we will not use proxy
			final List<Pattern> patterns = proxyConfiguration.getNoProxyHostPatterns();
			if (patterns != null) {
				for (final Pattern pattern : patterns) {
					final Matcher matcher = pattern.matcher(host);
					if (matcher.matches()) {
						return false;
					}
				}
			}
			return true;
		}
		return false;
	}
}
