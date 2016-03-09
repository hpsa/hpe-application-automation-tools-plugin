package com.hp.nga.integrations;

import com.hp.nga.integrations.api.CIPluginServices;
import com.hp.nga.integrations.services.bridge.BridgeService;
import com.hp.nga.integrations.services.logging.LoggingService;
import com.hp.nga.integrations.services.tasking.TasksProcessor;
import com.hp.nga.integrations.services.tasking.TasksProcessorImpl;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.util.Properties;

/**
 * Created by gullery on 22/01/2016.
 * <p/>
 * This class provides main entry point of interaction between an SDK and it's services and concrete plugin and it's services
 */

public class SDKManager {
	private static final Logger logger = LogManager.getLogger(SDKManager.class);
	private static final Integer API_VERSION = 1;
	private static CIPluginServices ciPluginServices;
	private static String sdkVersion;

	static {
		loadSDKProp();
	}

	private SDKManager() {
	}

	public static synchronized void init(CIPluginServices ciPluginServices) {
		if (ciPluginServices == null) {
			throw new IllegalArgumentException("SDK factory initialization failed: MUST be initialized with valid plugin services provider");
		}

		SDKManager.ciPluginServices = ciPluginServices;
		LoggingService.ensureInit();
		BridgeService.init();
		//  do init logic
		//  init bridge
		//  init rest client
	}


	public static Integer getAPIVersion() {
		return API_VERSION;
	}

	public static String getSDKVersion() {
		return sdkVersion;
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

	private static void loadSDKProp() {
		Properties p = new Properties();
		try {
			p.load(SDKManager.class.getClassLoader().getResourceAsStream("sdk.properties"));
			sdkVersion = p.getProperty("sdk.version");
		} catch (IOException ioe) {
			logger.error("failed to load SDK properties", ioe);
			sdkVersion = "";
		}
	}
}
