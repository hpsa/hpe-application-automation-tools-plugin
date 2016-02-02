package com.hp.octane.plugins.common.bridge;


import com.hp.octane.plugins.common.configuration.ServerConfiguration;
import com.hp.octane.plugins.jetbrains.teamcity.client.MqmRestClientFactory;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.logging.Logger;

/**
 * Created by gullery on 05/08/2015.
 * <p/>
 * Bridge Service meant to provide an abridged connectivity functionality
 * The only APIs to be exposed is the basic management of abridged clients
 */


public class BridgesService{
	private static final Logger logger = Logger.getLogger(BridgesService.class.getName());

	private static BridgesService instance;

	private BridgeClient bridgeClient;
	private String ciType;

	public static BridgesService getInstance() {
		if (instance == null) {
			instance = new BridgesService();
		}
		return instance;
	}

	public void updateBridge(ServerConfiguration conf) {
		if (isConfigurationValid(conf)) {
			if (bridgeClient != null) {
				bridgeClient.update(conf);
			} else {
				bridgeClient = new BridgeClient(conf, ciType);
			}
		} else {
			if (bridgeClient != null) {
				logger.info("BRIDGE: empty / non-valid configuration submitted, disposing bridge client");
				bridgeClient.dispose();
				bridgeClient = null;
			}
		}
	}

//	@Inject
//	public void setMqmRestClientFactory(TeamCityMqmRestClientFactory clientFactory) {
//		this.clientFactory = clientFactory;
//	}
	public void setCIType(String ciType) {
		this.ciType = ciType;
	}

//	@Override
//	public void onChanged(ServerConfiguration conf, ServerConfiguration oldConf) {
//		updateBridge(conf);
//	}

	private boolean isConfigurationValid(ServerConfiguration serverConfiguration) {
		boolean result = false;
		if (serverConfiguration.location != null && !serverConfiguration.location.isEmpty() &&
				serverConfiguration.sharedSpace != null && !serverConfiguration.sharedSpace.isEmpty()) {
			try {
				URL tmp = new URL(serverConfiguration.location);
				result = true;
			} catch (MalformedURLException mue) {
				logger.warning("BRIDGE: configuration with malformed URL supplied");
			}
		}
		return result;
	}
}
