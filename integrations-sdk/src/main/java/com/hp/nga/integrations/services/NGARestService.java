package com.hp.nga.integrations.services;

import com.hp.nga.integrations.SDKManager;
import com.hp.nga.integrations.SDKServiceInternal;
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
	private final SDKManager manager;
	private NGARestClient defaultClient;

	NGARestService(SDKManager manager) {
		this.manager = manager;
	}

	NGARestClient obtainClient() {
		if (defaultClient == null) {
			synchronized (DEFAULT_CLIENT_INIT_LOCK) {
				if (defaultClient == null) {
					defaultClient = new NGARestClient(manager);
				}
			}
		}
		return defaultClient;
	}

	NGARestClient createClient() {
		return new NGARestClient(manager);
	}
}
