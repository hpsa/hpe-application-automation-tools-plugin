package com.hp.mqm.opb.service.logging;

/**
 * Created with IntelliJ IDEA.
 * User: menashee
 * Date: 16/03/14
 *
 * Detailed logger per task, logs to a task specific file
 * The detailed log is intended for internal usage, all logs logged to the user logs will be added automatically to the detailed log
 */
public interface ContextDetailedLogger extends BaseTaskLogger {

    /**
     * Log a ERROR message, with associated Throwable information.
     * @param message the log message
     * @param thrown Throwable associated with the log message
     */
    void error(String message, Throwable thrown);

    /**
     * logs a DEBUG message
     * @param message the log message
     */
    void debug(String message);

    /**
     * logs a DEBUG message with parameters.
     * Each parameter should be a pair of curly braces with index starting from 0.
     * ie: logger.debug("a{0}c{1}2, "b", "1") will result in logging "abc12"
     * @param message the logs message
     * @param parameters the message parameters
     */
    void debug(String message, Object... parameters);

    /**
     * logs a FATAL message
     * @param message the log message
     */
    void fatal(String message);

    /**
     * logs a FATAL message with parameters.
     * Each parameter should be a pair of curly braces with index starting from 0.
     * ie: logger.fatal("a{0}c{1}2, "b", "1") will result in logging "abc12"
     * @param message the logs message
     * @param parameters the message parameters
     */
    void fatal(String message, Object... parameters);

    /**
     * Log a FATAL message, with associated Throwable information.
     * @param message the log message
     * @param thrown Throwable associated with the log message
     */
    void fatal(String message, Throwable thrown);
}
