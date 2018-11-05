package com.microfocus.application.automation.tools.common.masterToSlave;

import hudson.FilePath;
import hudson.remoting.VirtualChannel;
import jenkins.MasterToSlaveFileCallable;

import java.io.File;
import java.io.IOException;

/**
 * This class is suppose to be passed as argument to {@link hudson.FilePath#act(FilePath.FileCallable)} in order for
 * the master to execute a method that accepts parameter and returns parameter.
 * @param <T> The argument is returned for the method that is passed in the constructor.
 */
public class SupplierFileCallable<T> extends MasterToSlaveFileCallable<T> {
    private final ThrowingSupplier<T> supplier;

    public SupplierFileCallable(ThrowingSupplier<T> supplier) {
        this.supplier = supplier;
    }

    @Override
    public T invoke(File f, VirtualChannel channel) throws IOException, InterruptedException {
        return supplier.get();
    }
}