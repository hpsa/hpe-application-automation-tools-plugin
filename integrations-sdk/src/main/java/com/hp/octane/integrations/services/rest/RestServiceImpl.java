package com.hp.octane.integrations.services.rest;

import com.hp.octane.integrations.OctaneSDK;
import com.hp.octane.integrations.api.RestClient;
import com.hp.octane.integrations.api.RestService;
import com.hp.octane.integrations.dto.configuration.CIProxyConfiguration;
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

	public RestClient obtainClient() {
		if (defaultClient == null) {
			synchronized (DEFAULT_CLIENT_INIT_LOCK) {
				if (defaultClient == null) {
					defaultClient = new RestClientImpl(pluginServices);
				}
			}
		}
		return defaultClient;
	}

	public RestClient createClient(CIProxyConfiguration proxyConfiguration) {
		return new RestClientImpl(pluginServices);
	}

	@Override
	public void notifyConfigurationChange() {
		logger.info("connectivity configuration change has been notified; publishing to the RestClients");
		defaultClient.notifyConfigurationChange();
	}
}
