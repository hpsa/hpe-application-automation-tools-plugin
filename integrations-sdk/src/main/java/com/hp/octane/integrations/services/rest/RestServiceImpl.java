package com.hp.octane.integrations.services.rest;

import com.hp.octane.integrations.OctaneSDK;
import com.hp.octane.integrations.api.RestService;
import com.hp.octane.integrations.dto.configuration.CIProxyConfiguration;
import com.hp.octane.integrations.dto.configuration.OctaneConfiguration;
import com.hp.octane.integrations.spi.CIPluginServices;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Created by gullery on 14/01/2016.
 * <p/>
 * REST Service - default implementation
 */

public final class RestServiceImpl extends OctaneSDK.SDKServiceBase implements RestService {
	private static final Logger logger = LogManager.getLogger(RestServiceImpl.class);
	private static final Object DEFAULT_CLIENT_INIT_LOCK = new Object();

	private final CIPluginServices pluginServices;
	private RestClientImpl defaultClient;

	public RestServiceImpl(Object configurator, CIPluginServices pluginServices) {
		super(configurator);

		if (pluginServices == null) {
			throw new IllegalArgumentException("plugin services MUST NOT be null");
		}

		this.pluginServices = pluginServices;
	}

	public RestClientImpl obtainClient() {
		if (defaultClient == null) {
			synchronized (DEFAULT_CLIENT_INIT_LOCK) {
				if (defaultClient == null) {
					OctaneConfiguration octaneConfiguration = pluginServices.getOctaneConfiguration();
					CIProxyConfiguration proxyConfiguration = pluginServices.getProxyConfiguration(octaneConfiguration == null ? null : octaneConfiguration.getUrl());
					defaultClient = new RestClientImpl(pluginServices, proxyConfiguration);
				}
			}
		}
		return defaultClient;
	}

	public RestClientImpl createClient(CIProxyConfiguration proxyConfiguration) {
		return new RestClientImpl(pluginServices, proxyConfiguration);
	}
}
