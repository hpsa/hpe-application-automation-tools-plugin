package com.microfocus.application.automation.tools.common.masterToSlave;

import hudson.FilePath;
import hudson.remoting.VirtualChannel;
import jenkins.MasterToSlaveFileCallable;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;

/**
 * This class is suppose to be passed as argument to {@link hudson.FilePath#act(FilePath.FileCallable)} in order for
 * the master to execute a method that accepts parameter and returns parameter.
 * @param <T> The argument which passed to the generic method that will be executed on the slave.
 *            Note: it has to be serialized.
 * @param <R> The return value of the passed method in the constructor
 */
public class FunctionFileCallable<T extends Serializable, R> extends MasterToSlaveFileCallable<R> {
    private final ThrowingFunction<T, R> function;
    private final T value;

    public FunctionFileCallable(ThrowingFunction<T, R> function, T value) {
        this.function = function;
        this.value = value;
    }

    @Override
    public R invoke(File f, VirtualChannel channel) throws IOException, InterruptedException {
        return function.apply(value);
    }
}