package com.hp.mqm.atrf;

import com.hp.mqm.atrf.alm.services.AlmWrapperService;
import com.hp.mqm.atrf.core.configuration.CliParser;
import com.hp.mqm.atrf.core.configuration.FetchConfiguration;
import com.hp.mqm.atrf.octane.services.OctaneWrapperService;
import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.LoggerContext;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;

/**
 *
 */
public class Main {

    static final Logger logger = LogManager.getLogger();
    private static FetchConfiguration configuration;
    private static AlmWrapperService almWrapper;
    private static OctaneWrapperService octaneWrapper;

    public static void main(String[] args) {

        configureLog4J();
        setUncaughtExceptionHandler();

        CliParser cliParser = new CliParser();
        configuration = cliParser.parse(args);

        loginToAlm();
        loginToOctane();

        almWrapper.fetchRunsAndRelatedEntities(configuration);
    }

    private static void loginToAlm() {
        almWrapper = new AlmWrapperService(configuration.getAlmServerUrl(), configuration.getAlmDomain(), configuration.getAlmProject());
        if (almWrapper.login(configuration.getAlmUser(), configuration.getAlmPassword())) {

            logger.info("ALM : Login successful");
            if (almWrapper.validateConnectionToProject()) {
                logger.info("ALM : Connected to ALM project successfully");
            } else {
                throw new RuntimeException("ALM : Failed to connect to ALM Project.");
            }
        } else {
            throw new RuntimeException("ALM : Failed to login");
        }
    }

    private static void loginToOctane() {
        long sharedSpaceId = Long.parseLong(configuration.getOctaneSharedSpaceId());
        long workspaceId = Long.parseLong(configuration.getOctaneWorkspaceId());

        octaneWrapper = new OctaneWrapperService(configuration.getOctaneServerUrl(), sharedSpaceId, workspaceId);
        if (octaneWrapper.login(configuration.getOctaneUser(), configuration.getOctanePassword())) {

            logger.info("Octane : Login successful");
            if (octaneWrapper.validateConnectionToWorkspace()) {
                logger.info("Octane : Connected to Octane project successfully");
            } else {
                throw new RuntimeException("Octane : Failed to connect to Octane Workspace.");
            }
        } else {
            throw new RuntimeException("Octane : Failed to login");
        }
    }

    private static void setUncaughtExceptionHandler() {
        Thread.currentThread().setUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
            public void uncaughtException(Thread t, Throwable e) {
                logger.error(e.getMessage(), e);
            }
        });
    }

    private static void configureLog4J() {
        String log4jConfiguration = System.getProperty("log4j.configuration");
        if (StringUtils.isEmpty(log4jConfiguration)) {
            //try to take from file
            File f = new File("log4j2.xml");
            URI uri = null;
            if (f.exists() && !f.isDirectory() && f.canRead()) {
                uri = f.toURI();
            } else {
                //take it from resources
                try {
                    uri = Main.class.getClassLoader().getResource("log4j2.xml").toURI();
                } catch (URISyntaxException e) {
                    logger.info("Failed to load Log4j configuration loaded from resource file");
                }
            }

            LoggerContext context = (LoggerContext) LogManager.getContext(false);
            context.setConfigLocation(uri);
            logger.info("Log4j configuration loaded from " + uri.toString());
        } else {
            logger.info("Log4j configuration is loading from JVM argument log4j.configuration=" + log4jConfiguration);
        }
    }
}
