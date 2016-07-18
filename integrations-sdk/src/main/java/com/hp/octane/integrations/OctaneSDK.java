package com.hp.octane.integrations;

import com.hp.octane.integrations.api.CIPluginServices;
import com.hp.octane.integrations.api.ConfigurationService;
import com.hp.octane.integrations.api.EventsService;
import com.hp.octane.integrations.api.RestService;
import com.hp.octane.integrations.api.TasksProcessor;
import com.hp.octane.integrations.api.TestsService;
import com.hp.octane.integrations.services.bridge.BridgeService;
import com.hp.octane.integrations.services.configuration.ConfigurationServiceImpl;
import com.hp.octane.integrations.services.events.EventsServiceImpl;
import com.hp.octane.integrations.services.logging.LoggingService;
import com.hp.octane.integrations.services.rest.RestServiceImpl;
import com.hp.octane.integrations.services.tasking.TasksProcessorImpl;
import com.hp.octane.integrations.services.tests.TestsServiceImpl;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.util.Properties;

/**
 * Created by gullery on 22/01/2016.
 * <p/>
 * This class provides main entry point of interaction between an SDK and it's services and concrete plugin and it's services
 */

public final class OctaneSDK {
	private static final Logger logger = LogManager.getLogger(OctaneSDK.class);
	private static OctaneSDK instance;

	public static Integer API_VERSION;
	public static String SDK_VERSION;

	private final SDKConfigurator configurator;

	private OctaneSDK(CIPluginServices ciPluginServices) {
		initSDKProperties();
		configurator = new SDKConfigurator(ciPluginServices);
	}

	//  TODO: remove the boolean once migrated JP
	private static boolean initBridge;
	synchronized public static void init(CIPluginServices ciPluginServices, boolean initBridge) {
		if (ciPluginServices == null) {
			throw new IllegalArgumentException("SDK initialization failed: MUST be initialized with valid plugin services provider");
		}

		OctaneSDK.initBridge = initBridge;

		instance = new OctaneSDK(ciPluginServices);
	}

	public static OctaneSDK getInstance() {
		return instance;
	}

	public CIPluginServices getPluginServices() {
		return configurator.pluginServices;
	}

	public ConfigurationService getConfigurationService() {
		return configurator.configurationService;
	}

	public RestService getRestService() {
		return configurator.restService;
	}

	public TasksProcessor getTasksProcessor() {
		return configurator.tasksProcessor;
	}

	public EventsService getEventsService() {
		return configurator.eventsService;
	}

	public TestsService getTestsService() {
		return configurator.testsService;
	}

	private void initSDKProperties() {
		Properties p = new Properties();
		try {
			p.load(OctaneSDK.class.getClassLoader().getResourceAsStream("sdk.properties"));
		} catch (IOException ioe) {
			logger.error("SDK initialization failed: failed to load SDK properties", ioe);
			throw new IllegalStateException("SDK initialization failed: failed to load SDK properties", ioe);
		}
		if (!p.isEmpty()) {
			API_VERSION = Integer.parseInt(p.getProperty("api.version"));
			SDK_VERSION = p.getProperty("sdk.version");
		}
	}

	static class SDKConfigurator {
		private final CIPluginServices pluginServices;
		private LoggingService loggingService;
		private ConfigurationService configurationService;
		private RestService restService;
		private BridgeService bridgeService;
		private TasksProcessor tasksProcessor;
		private EventsService eventsService;
		private TestsService testsService;

		private SDKConfigurator(CIPluginServices pluginServices) {
			//  the order of services initialization below is important; please change with caution
			this.pluginServices = pluginServices;
			loggingService = new LoggingService(this);
			configurationService = new ConfigurationServiceImpl(this);
			restService = new RestServiceImpl(this);
			tasksProcessor = new TasksProcessorImpl(this);
			bridgeService = new BridgeService(this, initBridge);
			eventsService = new EventsServiceImpl(this);
			testsService = new TestsServiceImpl(this);
		}

		CIPluginServices getPluginServices() {
			return pluginServices;
		}

		RestService getRestService() {
			return restService;
		}

		TasksProcessor getTasksProcessor() {
			return tasksProcessor;
		}
	}
}
