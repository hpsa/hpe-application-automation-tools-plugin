package com.hp.octane.integrations.api;

import com.hp.octane.integrations.dto.configuration.CIProxyConfiguration;

public interface RestService {

	/**
	 * Retrieves default REST client: the one initialized with plugin's provided configuration and listening on it changes
	 *
	 * @return pre-configured RestClient
	 */
	RestClient obtainClient();

	/**
	 * Creates new REST client pre-configured with the specified configuration
	 *
	 * @param proxyConfiguration optional proxy configuration, if relevant
	 * @return pre-configured RestClient
	 */
	RestClient createClient(CIProxyConfiguration proxyConfiguration);

	/**
	 * Notifies the service that configuration has been changed
	 */
	void notifyConfigurationChange();
}
