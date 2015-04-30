package com.hp.mqm.opb.api;

import java.io.Serializable;

/**
 * The Agent Logger is a unified interface to log messages.
 * User: ginni
 * Date: 4/15/15
 */
public interface AgentLogger extends Serializable {


    /**
     * Logs message with DEBUG level.
     *
     * @param message The message to log
     */
    public void debug(Class clazz, String message);

    /**
     * Logs message with INFO level.
     *
     * @param message The message to log
     */
    public void info(Class clazz, String message);

    /**
     * Logs message with WARN level.
     *
     * @param message The message to log
     */
    public void warn(Class clazz, String message);

    /**
     * Logs message with ERROR level.
     *
     * @param message The message to log
     */
    public void error(Class clazz, String message);

    /**
     * Logs message with ERROR level and logs the stack trace of the Throwable
     * object.
     *
     * @param message The message to log
     * @param t       The exception object for which to log the stack trace.
     */
    public void error(Class clazz, String message, Throwable t);

    /**
     * Logs message with FATAL level.
     *
     * @param message The message to log
     */
    public void fatal(Class clazz, String message);

    /**
     * Logs message with FATAL level and the stack trace of Throwable object
     *
     * @param message The message to log
     * @param t       The exception object to log stack trace
     */
    public void fatal(Class clazz, String message, Throwable t);

    /**
     * Indicate whether debug level is enabled.
     *
     * @return true in case debug level is enabled and false otherwise.
     */
    public boolean isDebugEnabled(Class clazz);

}
