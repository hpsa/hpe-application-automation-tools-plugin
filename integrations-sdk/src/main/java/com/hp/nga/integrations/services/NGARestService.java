package com.hp.nga.integrations.services;

import com.hp.nga.integrations.SDKManager;
import com.hp.nga.integrations.SDKServiceInternal;
import com.hp.nga.integrations.dto.configuration.CIProxyConfiguration;
import com.hp.nga.integrations.dto.configuration.NGAConfiguration;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Created by gullery on 14/01/2016.
 * <p/>
 * REST Service - default implementation
 */

final class NGARestService implements SDKServiceInternal {
	private static final Logger logger = LogManager.getLogger(NGARestClient.class);
	private static final Object DEFAULT_CLIENT_INIT_LOCK = new Object();
	private final SDKManager sdk;
	private NGARestClient defaultClient;

	NGARestService(SDKManager sdk) {
		this.sdk = sdk;
	}

	NGARestClient obtainClient() {
		if (defaultClient == null) {
			synchronized (DEFAULT_CLIENT_INIT_LOCK) {
				if (defaultClient == null) {
					NGAConfiguration ngaConfiguration = sdk.getCIPluginServices().getNGAConfiguration();
					CIProxyConfiguration proxyConfiguration = sdk.getCIPluginServices().getProxyConfiguration(ngaConfiguration == null ? null : ngaConfiguration.getUrl());
					defaultClient = new NGARestClient(sdk, proxyConfiguration);
				}
			}
		}
		return defaultClient;
	}

	NGARestClient createClient(CIProxyConfiguration proxyConfiguration) {
		return new NGARestClient(sdk, proxyConfiguration);
	}
}
