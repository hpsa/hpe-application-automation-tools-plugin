package com.hp.nga.integrations.services;

import com.hp.nga.integrations.SDKManager;
import com.hp.nga.integrations.SDKServiceInternal;
import com.hp.octane.integrations.dto.configuration.CIProxyConfiguration;
import com.hp.octane.integrations.dto.configuration.OctaneConfiguration;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Created by gullery on 14/01/2016.
 * <p/>
 * REST Service - default implementation
 */

final class OctaneRestService implements SDKServiceInternal {
	private static final Logger logger = LogManager.getLogger(OctaneRestClient.class);
	private static final Object DEFAULT_CLIENT_INIT_LOCK = new Object();
	private final SDKManager sdk;
	private OctaneRestClient defaultClient;

	OctaneRestService(SDKManager sdk) {
		this.sdk = sdk;
	}

	OctaneRestClient obtainClient() {
		if (defaultClient == null) {
			synchronized (DEFAULT_CLIENT_INIT_LOCK) {
				if (defaultClient == null) {
					OctaneConfiguration octaneConfiguration = sdk.getCIPluginServices().getOctaneConfiguration();
					CIProxyConfiguration proxyConfiguration = sdk.getCIPluginServices().getProxyConfiguration(octaneConfiguration == null ? null : octaneConfiguration.getUrl());
					defaultClient = new OctaneRestClient(sdk, proxyConfiguration);
				}
			}
		}
		return defaultClient;
	}

	OctaneRestClient createClient(CIProxyConfiguration proxyConfiguration) {
		return new OctaneRestClient(sdk, proxyConfiguration);
	}
}
