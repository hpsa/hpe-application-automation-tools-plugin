package com.microfocus.application.automation.tools.common.masterToSlave;

import hudson.AbortException;

import java.io.Serializable;

/**
 * The interface was created in order to give a functional method like {@link java.util.function.Supplier} ability to
 * throw
 * additional
 * exceptions.
 * @param <T> The value which is returned from the method.
 */
@FunctionalInterface
public interface ThrowingSupplier<T> extends Serializable {
    T get() throws AbortException;
}