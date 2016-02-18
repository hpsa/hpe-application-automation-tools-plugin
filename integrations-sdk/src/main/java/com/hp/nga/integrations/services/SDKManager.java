package com.hp.nga.integrations.services;

import com.hp.nga.integrations.api.CIPluginServices;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Created by gullery on 22/01/2016.
 * <p>
 * This class provides main entry point of interaction between an SDK and it's services and concrete plugin and it's services
 */

public class SDKManager {
	private static final Logger logger = LogManager.getLogger(SDKManager.class);
	private static final Integer API_VERSION = 1;
	private static CIPluginServices ciPluginServices;

	private SDKManager() {
	}

	public static synchronized void init(CIPluginServices ciPluginServices) {
		if (ciPluginServices == null) {
			throw new IllegalArgumentException("SDK factory initialization failed: MUST be initialized with valid plugin services provider");
		}

		SDKManager.ciPluginServices = ciPluginServices;
		LoggingService.ensureInit();
		//  do init logic
		//  init bridge
		//  init rest client
	}

	public static Integer getApiVersion() {
		return API_VERSION;
	}

	public static CIPluginServices getCIPluginServices() {
		return ciPluginServices;
	}

	public static TasksProcessor getTasksProcessor() {
		ensureInitialization();
		return TasksProcessorImpl.getInstance();
	}

	private static void ensureInitialization() {
		if (ciPluginServices == null) {
			throw new IllegalStateException("SDK MUST be initialized prior to services consumption");
		}
	}
}
