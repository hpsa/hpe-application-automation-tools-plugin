package com.hp.octane.integrations.api;

import com.hp.octane.integrations.dto.configuration.CIProxyConfiguration;

public interface Rest_Service {

	/**
	 * Retrieves default REST client: the one initialized with plugin's provided configuration and listening on it changes
	 */
	RestClient obtainClient();

	/**
	 * Creates new REST client pre-configured with the specified configuration
	 *
	 * @param proxyConfiguration
	 * @return
	 */
	RestClient createClient(CIProxyConfiguration proxyConfiguration);
}
