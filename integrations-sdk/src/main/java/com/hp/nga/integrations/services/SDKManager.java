package com.hp.nga.integrations.services;

import com.hp.nga.integrations.api.CIPluginServices;
import com.hp.nga.integrations.api.ConfigurationService;
import com.hp.nga.integrations.api.EventsService;

import java.util.Properties;

/**
 * Created by gullery on 22/01/2016.
 * <p>
 * This class provides main entry point of interaction between an SDK and it's services and concrete plugin and it's services
 */

public class SDKManager {
	private static final Object INIT_CONFIGURATION_SERVICE_LOCK = new Object();
	private static final Object INIT_EVENTS_SERVICE_LOCK = new Object();
	private static final Integer API_VERSION = 1;
	private static CIPluginServices ciPluginServices;
	private static ConfigurationService configurationService;
	private static EventsService eventsService;
	private static String sdkVersion;

	static {
			//load api version
			loadSDKProp();
	};

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
	public static String getSDKVersion() {
		return sdkVersion;
	}

	public static CIPluginServices getCIPluginServices() {
		return ciPluginServices;
	}

	public static TasksProcessor getTasksProcessor() {
		return TasksProcessorImpl.getInstance();
	}

	public static ConfigurationService getConfigurationService() {
		ensureInitialization();
		if (configurationService == null) {
			synchronized (INIT_CONFIGURATION_SERVICE_LOCK) {
				if (configurationService == null) {
					configurationService = new ConfigurationServiceImpl();
				}
			}
		}
		return configurationService;
	}

	public static EventsService getEventsService() {
		ensureInitialization();
		if (eventsService == null) {
			synchronized (INIT_EVENTS_SERVICE_LOCK) {
				if (eventsService == null) {
					eventsService = new EventsServiceImpl();
				}
			}
		}
		return eventsService;
	}

	private static void ensureInitialization() {
		if (ciPluginServices == null) {
			throw new IllegalStateException("SDK MUST be initialized prior to services consumption");
		}
	}
	private static void loadSDKProp() {

		Properties p = new Properties();
		try {
			p.load(SDKManager.class.getClassLoader().getResourceAsStream("com/hp/nga/integrations/services/sdk.properties"));
			sdkVersion = p.getProperty("sdk.version");
		} catch (Exception e) {
			sdkVersion = "";
		}
	}
}
