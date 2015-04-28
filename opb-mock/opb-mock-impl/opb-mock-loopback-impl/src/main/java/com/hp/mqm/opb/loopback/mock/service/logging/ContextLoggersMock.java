package com.hp.mqm.opb.loopback.mock.service.logging;

import com.hp.mqm.opb.service.logging.ContextDetailedLogger;
import com.hp.mqm.opb.service.logging.ContextLoggers;
import com.hp.mqm.opb.service.logging.ContextProgressReporter;
import com.hp.mqm.opb.service.logging.ContextUserLogger;

/**
 * Created by ginni on 21/04/2015.
 *
 */
public class ContextLoggersMock implements ContextLoggers {
    ContextDetailedLogger contextDetailedLogger = new MockContextDetailedLogger();
    ContextUserLogger contextUserLogger = new MockContextUserLogger();

    @Override
    public ContextDetailedLogger getDetailedLogger() {
        return contextDetailedLogger;
    }

    @Override
    public ContextUserLogger getUserLogger() {
        return contextUserLogger;
    }

    @Override
    public ContextProgressReporter getContextProgressReporter() {
        return null;
    }

    @Override
    public void setContextProgressReporter(ContextProgressReporter contextProgressReporter) {

    }

    private class MockContextDetailedLogger implements ContextDetailedLogger {
        @Override
        public void error(String message, Throwable thrown) {
            System.out.println("Detailed Logger: error : " + message);
            thrown.printStackTrace();
        }

        @Override
        public void debug(String message) {
            System.out.println("Detailed Logger: debug : " + message);
        }

        @Override
        public void debug(String message, Object... parameters) {
            System.out.println("Detailed Logger: debug : " + message);
        }

        @Override
        public void fatal(String message) {
            System.out.println("Detailed Logger: debug : " + message);
        }

        @Override
        public void fatal(String message, Object... parameters) {
            System.out.println("Detailed Logger: fatal : " + message);
        }

        @Override
        public void fatal(String message, Throwable thrown) {
            System.out.println("Detailed Logger: fatal : " + message);
            thrown.printStackTrace();
        }

        @Override
        public void info(String message) {
            System.out.println("Detailed Logger: info : " + message);
        }

        @Override
        public void info(String message, Object... parameters) {
            System.out.println("Detailed Logger: info : " + message);
        }

        @Override
        public void warn(String message) {
            System.out.println("Detailed Logger: warning : " + message);
        }

        @Override
        public void warn(String message, Object... parameters) {
            System.out.println("Detailed Logger: warning : " + message);
        }

        @Override
        public void error(String message) {
            System.out.println("Detailed Logger: error : " + message);
        }

        @Override
        public void error(String message, Object... parameters) {
            System.out.println("Detailed Logger: error : " + message);
        }
    }

    private class MockContextUserLogger implements ContextUserLogger {
        @Override
        public void info(String message) {
            System.out.println("User Logger: info : " + message);
        }

        @Override
        public void info(String message, Object... parameters) {
            System.out.println("User Logger: info : " + message);
        }

        @Override
        public void warn(String message) {
            System.out.println("User Logger: warning : " + message);
        }

        @Override
        public void warn(String message, Object... parameters) {
            System.out.println("User Logger: warning : " + message);
        }

        @Override
        public void error(String message) {
            System.out.println("User Logger: error : " + message);
        }

        @Override
        public void error(String message, Object... parameters) {
            System.out.println("User Logger: error : " + message);
        }
    }

    @Override
    public void markToRemoveLog() {

    }

    @Override
    public boolean isMarkToRemoveLog() {
        return false;
    }
}
