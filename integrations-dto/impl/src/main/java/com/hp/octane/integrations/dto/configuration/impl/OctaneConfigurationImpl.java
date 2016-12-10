package com.hp.octane.integrations.dto.configuration.impl;

import com.hp.octane.integrations.dto.configuration.OctaneConfiguration;

import java.net.MalformedURLException;
import java.net.URL;

/**
 * Created by gullery on 08/01/2016.
 * <p/>
 * Octane Server configuration descriptor
 */

class OctaneConfigurationImpl implements OctaneConfiguration {
	private String url;
	private String sharedSpace;
	private String apiKey;
	private String secret;

	public String getUrl() {
		return url;
	}

	public OctaneConfiguration setUrl(String url) {
		this.url = url;
		return this;
	}

	public String getSharedSpace() {
		return sharedSpace;
	}

	public OctaneConfiguration setSharedSpace(String sharedSpace) {
		this.sharedSpace = sharedSpace;
		return this;
	}

	public String getApiKey() {
		return apiKey;
	}

	public OctaneConfiguration setApiKey(String apiKey) {
		this.apiKey = apiKey;
		return this;
	}

	public String getSecret() {
		return secret;
	}

	public OctaneConfiguration setSecret(String secret) {
		this.secret = secret;
		return this;
	}

	public boolean isValid() {
		boolean result = false;
		if (url != null && !url.isEmpty() && sharedSpace != null && !sharedSpace.isEmpty()) {
			try {
				URL tmp = new URL(url);
				result = true;
			} catch (MalformedURLException mue) {
				result = false;
			}
		}
		return result;
	}

	@Override
	public String toString() {
		return "NGAConfigurationImpl { " +
				"url: " + url +
				", sharedSpace: " + sharedSpace +
				", apiKey: " + apiKey + " }";
	}
}
