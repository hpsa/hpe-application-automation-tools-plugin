package com.hp.mqm.opb.service.logging;

/**
 * Basic operations for Task logger
 *
 * @author eladmena
 */
public interface BaseTaskLogger {

    /**
     * logs a INFO message
     * @param message the log message
     */
    void info(String message);

    /**
     * logs a INFO message with parameters.
     * Each parameter should be a pair of curly braces with index starting from 0.
     * ie: logger.info("a{0}c{1}2, "b", "1") will result in logging "abc12"
     * @param message the logs message
     * @param parameters the message parameters
     */
    void info(String message, Object... parameters);

    /**
     * logs a WARN message
     * @param message the log message
     */
    void warn(String message);

    /**
     * logs a WARN message with parameters.
     * Each parameter should be a pair of curly braces with index starting from 0.
     * ie: logger.warn("a{0}c{1}2, "b", "1") will result in logging "abc12"
     * @param message the logs message
     * @param parameters the message parameters
     */
    void warn(String message, Object... parameters);

    /**
     * logs a ERROR message
     * @param message the log message
     */
    void error(String message);

    /**
     * logs a ERROR message with parameters.
     * Each parameter should be a pair of curly braces with index starting from 0.
     * ie: logger.error("a{0}c{1}2, "b", "1") will result in logging "abc12"
     * @param message the logs message
     * @param parameters the message parameters
     */
    void error(String message, Object... parameters);
}
