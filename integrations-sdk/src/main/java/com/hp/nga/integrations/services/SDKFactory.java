package com.hp.nga.integrations.services;

import com.hp.nga.integrations.api.CIPluginServices;
import com.hp.nga.integrations.api.SDKServicesProvider;

/**
 * Created by gullery on 22/01/2016.
 * <p>
 * This class provides main entry point of interaction between an SDK and it's services and concrete plugin and it's services
 */

public class SDKFactory {
	private static final Object INIT_LOCK = new Object();
	private static CIPluginServices ciPluginServices;
	private static SDKServicesProvider sdkServicesProvider;

	private SDKFactory() {
	}

	public static synchronized void init(CIPluginServices ciPluginServices) {
		if (ciPluginServices == null) {
			throw new IllegalArgumentException("SDK factory initialization failed: MUST be initialized with valid plugin services provider");
		}

		SDKFactory.ciPluginServices = ciPluginServices;
		//  do init logic
		//  init bridge
		//  init rest client
	}

	public static SDKServicesProvider getSDKServicesProvider() {
		ensureInitialization();
		if (sdkServicesProvider == null) {
			synchronized (INIT_LOCK) {
				if (sdkServicesProvider == null) {
					sdkServicesProvider = new SDKServicesProviderImpl(ciPluginServices);
				}
			}
		}
		return sdkServicesProvider;
	}

	private static void ensureInitialization() {
		if (ciPluginServices == null) {
			throw new IllegalStateException("SDK MUST be properly initialized prior to consumption");
		}
	}
}
