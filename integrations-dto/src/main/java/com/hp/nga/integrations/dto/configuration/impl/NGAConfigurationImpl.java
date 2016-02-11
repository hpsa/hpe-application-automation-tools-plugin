package com.hp.nga.integrations.dto.configuration.impl;

import com.hp.nga.integrations.dto.configuration.NGAConfiguration;

import java.net.MalformedURLException;
import java.net.URL;

/**
 * Created by gullery on 08/01/2016.
 * <p>
 * NGA Server configuration descriptor
 */

class NGAConfigurationImpl implements NGAConfiguration {
	private String url;
	private Long sharedSpace;
	private String clientId;
	private String apiKey;

	public String getUrl() {
		return url;
	}

	public NGAConfiguration setUrl(String url) {
		this.url = url;
		return this;
	}

	public Long getSharedSpace() {
		return sharedSpace;
	}

	public NGAConfiguration setSharedSpace(Long sharedSpace) {
		this.sharedSpace = sharedSpace;
		return this;
	}

	public String getClientId() {
		return clientId;
	}

	public NGAConfiguration setClientId(String clientId) {
		this.clientId = clientId;
		return this;
	}

	public String getApiKey() {
		return apiKey;
	}

	public NGAConfiguration setApiKey(String apiKey) {
		this.apiKey = apiKey;
		return this;
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
