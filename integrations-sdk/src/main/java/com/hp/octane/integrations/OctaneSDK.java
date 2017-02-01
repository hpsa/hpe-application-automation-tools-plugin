package com.hp.octane.integrations;

import com.hp.octane.integrations.api.*;
import com.hp.octane.integrations.services.bridge.BridgeServiceImpl;
import com.hp.octane.integrations.services.configuration.ConfigurationServiceImpl;
import com.hp.octane.integrations.services.events.EventsServiceImpl;
import com.hp.octane.integrations.services.logging.LoggingService;
import com.hp.octane.integrations.services.rest.RestServiceImpl;
import com.hp.octane.integrations.services.tasking.TasksProcessorImpl;
import com.hp.octane.integrations.services.tests.TestsServiceImpl;
import com.hp.octane.integrations.spi.CIPluginServices;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.util.Properties;

/**
 * Created by gullery on 22/01/2016.
 * <p>
 * This class provides main entry point of interaction between an SDK and it's services and concrete plugin and it's services
 */

public final class OctaneSDK {
	private static final Logger logger = LogManager.getLogger(OctaneSDK.class);
	private static volatile OctaneSDK instance;

	public static Integer API_VERSION;
	public static String SDK_VERSION;

	private final SDKConfigurator configurator;

	private OctaneSDK(CIPluginServices ciPluginServices) {
		initSDKProperties();
		configurator = new SDKConfigurator(ciPluginServices);
		setPredictiveKeyPath(ciPluginServices);
	}

	//  TODO: remove the boolean once migrated JP
	private static boolean initBridge;

	synchronized public static void init(CIPluginServices ciPluginServices, boolean initBridge) {
		if (instance == null) {
			if (ciPluginServices == null) {
				throw new IllegalArgumentException("SDK initialization failed: MUST be initialized with valid plugin services provider");
			}
			OctaneSDK.initBridge = initBridge;
			instance = new OctaneSDK(ciPluginServices);
			logger.info("SDK has been initialized");
		} else {
			logger.error("SDK may be initialized only once, secondary initialization attempt encountered");
		}
	}

	public static OctaneSDK getInstance() {
		if (instance != null) {
			return instance;
		} else {
			throw new IllegalStateException("SDK MUST be initialized prior to any usage");
		}
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

	private void setPredictiveKeyPath(CIPluginServices ciPluginServices) {
		File file = new File(Jenkins.getInstance().getRootDir());
		if (file != null && (file.isDirectory() || !file.exists())) {
			System.setProperty("pem_file", file.getAbsolutePath() + File.separator
					+ "keys" + File.separator + "predictive.pem");
		}
	}

	private static class SDKConfigurator {
		private final CIPluginServices pluginServices;
		private final LoggingService loggingService;
		private final RestService restService;
		private final ConfigurationService configurationService;
		private final BridgeServiceImpl bridgeServiceImpl;
		private final TasksProcessor tasksProcessor;
		private final EventsService eventsService;
		private final TestsService testsService;

		private SDKConfigurator(CIPluginServices pluginServices) {
			this.pluginServices = pluginServices;
			loggingService = new LoggingService(this, pluginServices);
			restService = new RestServiceImpl(this, pluginServices);
			tasksProcessor = new TasksProcessorImpl(this, pluginServices);
			configurationService = new ConfigurationServiceImpl(this, pluginServices, restService);
			eventsService = new EventsServiceImpl(this, pluginServices, restService);
			testsService = new TestsServiceImpl(this, pluginServices, restService);
			bridgeServiceImpl = new BridgeServiceImpl(this, pluginServices, restService, tasksProcessor, initBridge);
		}
	}

	//  the below base class used ONLY for a correct initiation enforcement of an SDK services
	public static abstract class SDKServiceBase {
		protected SDKServiceBase(Object configurator) {
			if (configurator == null) {
				throw new IllegalArgumentException("configurator MUST NOT be null");
			}
			if (!(configurator instanceof SDKConfigurator)) {
				throw new IllegalArgumentException("configurator MUST be of a correct type");
			}
		}
	}
}
