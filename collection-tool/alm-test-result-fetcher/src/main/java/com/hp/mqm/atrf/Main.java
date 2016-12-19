package com.hp.mqm.atrf;

import com.hp.mqm.atrf.core.configuration.CliParser;
import com.hp.mqm.atrf.core.configuration.FetchConfiguration;
import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.LoggerContext;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.DateFormat;
import java.util.Date;
import java.util.Locale;

/**
 *
 */
public class Main {

    static final Logger logger = LogManager.getLogger();


    public static void main(String[] args) {

        long start = System.currentTimeMillis();
        configureLog4J();
        setUncaughtExceptionHandler();

        logger.info("***************************************************************************************************");

        DateFormat dateFormatter = DateFormat.getDateInstance(DateFormat.DEFAULT, Locale.getDefault());
        DateFormat timeFormatter = DateFormat.getTimeInstance(DateFormat.DEFAULT, Locale.getDefault());
        logger.info((String.format("Starting fetch process %s %s", dateFormatter.format(new Date()), timeFormatter.format(new Date()))));
        logger.info("***************************************************************************************************");

        CliParser cliParser = new CliParser();
        FetchConfiguration configuration = cliParser.parse(args);
        App app = new App(configuration);
        app.start();

        long end = System.currentTimeMillis();
        logger.info(String.format("Fetch process is finished in %s seconds", (end - start)/1000));
        logger.info("***************************************************************************************************");
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
            logger.info("Log4j configuration is loading from log4j.configuration=" + log4jConfiguration);
        }
    }
}
