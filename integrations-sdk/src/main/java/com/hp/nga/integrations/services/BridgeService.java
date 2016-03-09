package com.hp.nga.integrations.services;

import com.hp.nga.integrations.SDKManager;
import com.hp.nga.integrations.SDKServiceInternal;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Created by gullery on 05/08/2015.
 * <p/>
 * Bridge Service meant to provide an abridged connectivity functionality
 * The only APIs to be exposed is the basic management of abridged client/s
 */

final class BridgeService implements SDKServiceInternal {
	private static final Logger logger = LogManager.getLogger(BridgeService.class);
	private BridgeClient bridgeClient;

	BridgeService(SDKManager manager, boolean startBridge) {
		if (startBridge) {
			bridgeClient = new BridgeClient(manager);
		}
	}
}
