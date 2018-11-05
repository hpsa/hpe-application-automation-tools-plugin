package com.microfocus.application.automation.tools.common.masterToSlave;

import hudson.AbortException;
import java.io.Serializable;


/**
 * The interface was created in order to give a functional method like {@link java.util.function.Function} to throw
 * additional
 * exceptions
 * @param <T> The value which is passed to the method.
 * @param <R> The value which is retuend from the method.
 */
@FunctionalInterface
public interface ThrowingFunction<T, R> extends Serializable {
    R apply(T t) throws AbortException, InterruptedException;
}
