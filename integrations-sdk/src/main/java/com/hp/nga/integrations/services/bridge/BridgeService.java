package com.hp.nga.integrations.services.bridge;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Created by gullery on 05/08/2015.
 * <p/>
 * Bridge Service meant to provide an abridged connectivity functionality
 * The only APIs to be exposed is the basic management of abridged client/s
 */

public class BridgeService {
	private static final Logger logger = LogManager.getLogger(BridgeService.class);
	private static final Object BRIDGE_INIT_LOCK = new Object();
	private BridgeClient bridgeClient;

	private BridgeService() {
	}

	public static void init() {
		INSTANCE_HOLDER.instance.bridgeClient = new BridgeClient();
	}

	private static final class INSTANCE_HOLDER {
		private static final BridgeService instance = new BridgeService();
	}
}
