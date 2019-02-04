/*
 * Certain versions of software and/or documents ("Material") accessible here may contain branding from
 * Hewlett-Packard Company (now HP Inc.) and Hewlett Packard Enterprise Company.  As of September 1, 2017,
 * the Material is now offered by Micro Focus, a separately owned and operated company.  Any reference to the HP
 * and Hewlett Packard Enterprise/HPE marks is historical in nature, and the HP and Hewlett Packard Enterprise/HPE
 * marks are the property of their respective owners.
 * __________________________________________________________________
 * MIT License
 *
 * (c) Copyright 2012-2019 Micro Focus or one of its affiliates.
 *
 * The only warranties for products and services of Micro Focus and its affiliates
 * and licensors ("Micro Focus") are set forth in the express warranty statements
 * accompanying such products and services. Nothing herein should be construed as
 * constituting an additional warranty. Micro Focus shall not be liable for technical
 * or editorial errors or omissions contained herein.
 * The information contained herein is subject to change without notice.
 * ___________________________________________________________________
 */

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