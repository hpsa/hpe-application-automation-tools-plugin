package com.hp.nga.integrations.services.bridge;

import com.hp.nga.integrations.services.configuration.NGAConfiguration;

import java.util.logging.Logger;

/**
 * Created by gullery on 05/08/2015.
 * <p/>
 * Bridge Service meant to provide an abridged connectivity functionality
 * The only APIs to be exposed is the basic management of abridged clients
 */

public class BridgeService {
	private static final Logger logger = Logger.getLogger(BridgeService.class.getName());

	private static final BridgeService instance = new BridgeService();
	private BridgeClient bridgeClient;

	private BridgeService() {
	}

	public static BridgeService getInstance() {
		return instance;
	}

	public void updateBridge(NGAConfiguration config) {
		if (config.isValid()) {
			if (bridgeClient != null) {
				bridgeClient.update(config);
			} else {
				bridgeClient = new BridgeClient();
			}
		} else {
			if (bridgeClient != null) {
				logger.info("BRIDGE: empty / non-valid configuration submitted, disposing bridge client");
				bridgeClient.dispose();
				bridgeClient = null;
			}
		}
	}
}
