package com.hp.nga.integrations.services;

import com.hp.nga.integrations.api.CIPluginService;
import com.hp.nga.integrations.api.SDKServicesProvider;

/**
 * Created by gullery on 22/01/2016.
 * <p>
 * This class provides main entry point of interaction between an SDK and it's services and concrete plugin and it's services
 */

public class SDKFactory {
	private static final Object INIT_LOCK = new Object();
	private static CIPluginService ciPluginService;
	private static SDKServicesProvider sdkServicesProvider;

	private SDKFactory() {
	}

	public static synchronized void init(CIPluginService ciPluginService) {
		if (ciPluginService == null) {
			throw new IllegalArgumentException("SDK factory initialization failed: MUST be initialized with valid plugin services provider");
		}

		SDKFactory.ciPluginService = ciPluginService;
	}

	public static SDKServicesProvider getSDKServicesProvider() {
		ensureInitialization();
		if (sdkServicesProvider == null) {
			synchronized (INIT_LOCK) {
				if (sdkServicesProvider == null) {
					sdkServicesProvider = new SDKServicesProviderImpl(ciPluginService);
				}
			}
		}
		return sdkServicesProvider;
	}

	private static void ensureInitialization() {
		if (ciPluginService == null) {
			throw new IllegalStateException("SDK MUST be properly initialized prior to consumption");
		}
	}
}
