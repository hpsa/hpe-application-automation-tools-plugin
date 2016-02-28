package com.hp.nga.integrations.services;

import com.hp.nga.integrations.api.CIPluginServices;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.Appender;
import org.apache.logging.log4j.core.Layout;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.appender.RollingFileAppender;
import org.apache.logging.log4j.core.appender.rolling.DefaultRolloverStrategy;
import org.apache.logging.log4j.core.appender.rolling.RollingFileManager;
import org.apache.logging.log4j.core.appender.rolling.SizeBasedTriggeringPolicy;
import org.apache.logging.log4j.core.config.AppenderRef;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.LoggerConfig;
import org.apache.logging.log4j.core.layout.PatternLayout;

import java.io.File;

/**
 * Created by gullery on 14/02/2016.
 * <p>
 * Service for management logging capabilities of the plugin (SDK); currently meant for the internal usage only
 */

class LoggingService {
	private static final Object INIT_LOCKER = new Object();
	private static final String LOGS_LOCATION = "logs" + File.separator + "nga.log";

	private final CIPluginServices pluginServices;
	private Level logLevel = Level.INFO;

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
				Configuration config = ctx.getConfiguration();
				Layout layout = PatternLayout.newBuilder().withConfiguration(config).withPattern(PatternLayout.SIMPLE_CONVERSION_PATTERN).build();
				SizeBasedTriggeringPolicy policy = SizeBasedTriggeringPolicy.createPolicy("2MB");
				DefaultRolloverStrategy strategy = DefaultRolloverStrategy.createStrategy("10", "0", "min", null, null, true, config);
				RollingFileManager fileManager = RollingFileManager.getFileManager(
						new File(pluginServices.getAllowedNGAStorage(), LOGS_LOCATION).getAbsolutePath(),
						new File(pluginServices.getAllowedNGAStorage(), LOGS_LOCATION).getAbsolutePath() + "%i",
						false,
						false,
						policy,
						strategy,
						null,
						layout,
						128);
				policy.initialize(fileManager);
				Appender appender = RollingFileAppender.createAppender(
						new File(pluginServices.getAllowedNGAStorage(), LOGS_LOCATION).getAbsolutePath(),
						new File(pluginServices.getAllowedNGAStorage(), LOGS_LOCATION).getAbsolutePath() + "%i",
						"true",
						"NGAFileLogger",
						"false",
						"128",
						"true",
						policy,
						strategy,
						layout,
						null,
						"false",
						"false",
						null,
						config);
				appender.start();
				config.addAppender(appender);
				AppenderRef ref = AppenderRef.createAppenderRef("NGAFileLogger", null, null);
				AppenderRef[] refs = new AppenderRef[]{ref};
				LoggerConfig loggerConfig = LoggerConfig.createLogger(
						"true",
						logLevel,
						LogManager.ROOT_LOGGER_NAME,
						"true",
						refs,
						null,
						config,
						null);
				loggerConfig.addAppender(appender, logLevel, null);
				config.addLogger(LogManager.ROOT_LOGGER_NAME, loggerConfig);
				ctx.updateLoggers();
			}
		}
	}

	private static final class INSTANCE_HOLDER {
		private static final LoggingService instance = new LoggingService();
	}
}
