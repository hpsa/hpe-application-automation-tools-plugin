package com.hp.octane.integrations.services.rest;

import com.hp.octane.integrations.SDKServiceBase;
import com.hp.octane.integrations.api.RestService;
import com.hp.octane.integrations.dto.configuration.CIProxyConfiguration;
import com.hp.octane.integrations.dto.configuration.OctaneConfiguration;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Created by gullery on 14/01/2016.
 * <p/>
 * REST Service - default implementation
 */

public final class RestServiceImpl extends SDKServiceBase implements RestService {
	private static final Logger logger = LogManager.getLogger(RestServiceImpl.class);
	private static final Object DEFAULT_CLIENT_INIT_LOCK = new Object();
	private RestClientImpl defaultClient;

	public RestServiceImpl(Object configurator) {
		super(configurator);
	}

	public RestClientImpl obtainClient() {
		if (defaultClient == null) {
			synchronized (DEFAULT_CLIENT_INIT_LOCK) {
				if (defaultClient == null) {
					OctaneConfiguration octaneConfiguration = getPluginServices().getOctaneConfiguration();
					CIProxyConfiguration proxyConfiguration = getPluginServices().getProxyConfiguration(octaneConfiguration == null ? null : octaneConfiguration.getUrl());
					defaultClient = new RestClientImpl(getPluginServices(), proxyConfiguration);
				}
			}
		}
		return defaultClient;
	}

	public RestClientImpl createClient(CIProxyConfiguration proxyConfiguration) {
		return new RestClientImpl(getPluginServices(), proxyConfiguration);
	}
}
