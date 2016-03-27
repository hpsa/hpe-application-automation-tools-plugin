package com.hp.nga.integrations;

import com.hp.nga.integrations.api.CIPluginServices;
import com.hp.nga.integrations.services.SDKServicesProvider;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * Created by gullery on 22/01/2016.
 * <p/>
 * This class provides main entry point of interaction between an SDK and it's services and concrete plugin and it's services
 */

public final class SDKManager {
	private static final Logger logger = LogManager.getLogger(SDKManager.class);
	public static Integer API_VERSION;
	public static String SDK_VERSION;
	public final List<SDKServicePublic> publicList = new ArrayList<SDKServicePublic>();
	public final List<SDKServiceInternal> internalList = new ArrayList<SDKServiceInternal>();
	private CIPluginServices ciPluginServices;

	private SDKManager() {
		Properties p = new Properties();
		try {
			p.load(SDKManager.class.getClassLoader().getResourceAsStream("sdk.properties"));
		} catch (IOException ioe) {
			logger.error("failed to load SDK properties", ioe);
			throw new IllegalStateException("failed to load SDK properties", ioe);
		}
		if (!p.isEmpty()) {
			API_VERSION = Integer.parseInt(p.getProperty("api.version"));
			SDK_VERSION = p.getProperty("sdk.version");
		}
	}

	//  TODO: remove boolean flag when finally migrated to the new client
	public static void init(CIPluginServices ciPluginServices, boolean startBridge) {
		if (ciPluginServices == null) {
			throw new IllegalArgumentException("SDK factory initialization failed: MUST be initialized with valid plugin services provider");
		}

		INSTANCE_HOLDER.instance.ciPluginServices = ciPluginServices;
		new SDKServicesProvider().registerServices(INSTANCE_HOLDER.instance, startBridge);
	}

	public static <T extends SDKServicePublic> T getService(Class<T> type) {
		if (INSTANCE_HOLDER.instance.ciPluginServices == null) {
			throw new IllegalStateException("SDK MUST be initialized prior to services consumption");
		}

		for (SDKServicePublic s : INSTANCE_HOLDER.instance.publicList) {
			if (type.isAssignableFrom(s.getClass())) {
				return (T) s;
			}
		}
		throw new IllegalArgumentException(type + " type is not a known public service");
	}

	public <T extends SDKServiceInternal> T getInternalService(Class<T> type) {
		if (INSTANCE_HOLDER.instance.ciPluginServices == null) {
			throw new IllegalStateException("SDK MUST be initialized prior to services consumption");
		}

		for (SDKServiceInternal s : INSTANCE_HOLDER.instance.internalList) {
			if (s.getClass().isAssignableFrom(type)) {
				return (T) s;
			}
		}
		throw new IllegalArgumentException(type + " type is not a known internal service");
	}

	public CIPluginServices getCIPluginServices() {
		return ciPluginServices;
	}

	private static final class INSTANCE_HOLDER {
		private static final SDKManager instance = new SDKManager();
	}
}
