package com.hp.nga.integrations.api;

import com.hp.nga.integrations.SDKServicePublic;
import com.hp.nga.integrations.dto.configuration.CIProxyConfiguration;
import com.hp.nga.integrations.dto.configuration.NGAConfiguration;
import com.hp.nga.integrations.dto.connectivity.NGAResponse;

public interface ConfigurationService extends SDKServicePublic {

	/**
	 * Tests connectivity to the NGA server with the supplied configuration
	 *
	 * @param configuration
	 * @return
	 * @throws RuntimeException in case of connection failure
	 */
	NGAResponse testConnection(NGAConfiguration configuration) throws RuntimeException;

	/**
	 * Notify SDK notification on NGA configuration change
	 *
	 * @param newConfiguration
	 */
	void notifyChange(NGAConfiguration newConfiguration);

	/**
	 * Notify SDK about proxy configuration change of the hosting CI Server
	 *
	 * @param newConfiguration
	 */
	void notifyChange(CIProxyConfiguration newConfiguration);
}
