package com.hp.nga.integrations.services;

import com.hp.nga.integrations.api.CIPluginServices;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.LoggerContext;

import java.io.File;

/**
 * Created by gullery on 14/02/2016.
 * <p/>
 * Service for management logging capabilities of the plugin (SDK); currently meant for the internal usage only
 */

class LoggingService {
	private static final Object INIT_LOCKER = new Object();
	private static final String LOGS_LOCATION_SYS_PROPERTY = "ngaLogsLocation";
	private static final String LOGS_LOCATION_SUB_FOLDER = "logs";

	private final CIPluginServices pluginServices;

	private LoggingService() {
		pluginServices = SDKManager.getCIPluginServices();
		configureLogger();
	}

	static void ensureInit() {
		if (INSTANCE_HOLDER.instance == null) {
			throw new IllegalStateException("Logging service was not initialized properly");
		}
	}

	private void configureLogger() {
		File file = pluginServices.getAllowedNGAStorage();
		if (file != null && (file.isDirectory() || !file.exists())) {
			synchronized (INIT_LOCKER) {
				LoggerContext ctx = (LoggerContext) LogManager.getContext(false);
				System.setProperty(LOGS_LOCATION_SYS_PROPERTY, new File(file, LOGS_LOCATION_SUB_FOLDER).getAbsolutePath());
				ctx.reconfigure();
			}
		}
	}

	private static final class INSTANCE_HOLDER {
		private static final LoggingService instance = new LoggingService();
	}
}
