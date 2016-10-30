package com.hp.octane.integrations.api;

import com.hp.octane.integrations.dto.configuration.CIProxyConfiguration;
import com.hp.octane.integrations.dto.configuration.OctaneConfiguration;
import com.hp.octane.integrations.dto.connectivity.OctaneResponse;

import java.io.IOException;

public interface ConfigurationService {

	/**
	 * Builds configuration object from raw data, usually supplied from UI or storage
	 *
	 * @param rawUrl
	 * @param apiKey
	 * @param secret
	 * @return
	 */
	OctaneConfiguration buildConfiguration(String rawUrl, String apiKey, String secret) throws IllegalArgumentException;

	/**
	 * Tests connectivity to the NGA server with the supplied configuration
	 *
	 * @param configuration
	 * @return
	 * @throws IOException in case of connection failure
	 */
	OctaneResponse validateConfiguration(OctaneConfiguration configuration) throws IOException;

	/**
	 * Notify SDK notification on NGA configuration change
	 */
	void notifyChange();
}
