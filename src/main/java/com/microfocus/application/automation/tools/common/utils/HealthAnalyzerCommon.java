/*
 * Certain versions of software and/or documents ("Material") accessible here may contain branding from
 * Hewlett-Packard Company (now HP Inc.) and Hewlett Packard Enterprise Company.  As of September 1, 2017,
 * the Material is now offered by Micro Focus, a separately owned and operated company.  Any reference to the HP
 * and Hewlett Packard Enterprise/HPE marks is historical in nature, and the HP and Hewlett Packard Enterprise/HPE
 * marks are the property of their respective owners.
 * __________________________________________________________________
 * MIT License
 *
 * (c) Copyright 2012-2021 Micro Focus or one of its affiliates.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated
 * documentation files (the "Software"), to deal in the Software without restriction, including without limitation
 * the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software,
 * and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or
 * substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO
 * THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT,
 * TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 *
 * ___________________________________________________________________
 */

package com.microfocus.application.automation.tools.common.utils;

import com.microfocus.application.automation.tools.common.Messages;
import com.microfocus.application.automation.tools.common.masterToSlave.FunctionFileCallable;
import com.microfocus.application.automation.tools.common.masterToSlave.SupplierFileCallable;
import com.microfocus.application.automation.tools.common.model.VariableWrapper;
import hudson.AbortException;
import hudson.FilePath;

import javax.annotation.Nonnull;
import java.io.*;
import java.nio.file.Paths;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Stream;

/**
 * The HealthAnalyzerCommon provides generic methods for every product to use
 * Requires to be initiated with the product name
 */
public class HealthAnalyzerCommon implements Serializable {
    private static final String AN_EXCEPTION_WAS_THROWN = "An exception was thrown";
    private static final Logger logger = Logger.getLogger(HealthAnalyzerCommon.class.getName());
    private final String productName;

    public HealthAnalyzerCommon(@Nonnull final String productName) {
        Objects.requireNonNull(productName, Messages.HealthAnalyzerCommon_productNameValueMustBeNotNull());
        this.productName = productName;
    }

    public void ifCheckedPerformWindowsInstallationCheck(@Nonnull final String registryPath, final boolean toCheck,
                                                         FilePath workspace)
            throws IOException, InterruptedException {
        if (toCheck) {
            Objects.requireNonNull(registryPath, Messages.HealthAnalyzerCommon_registryValueMustBeNotNull());

            if (!isRegistryExistsOnSlave(registryPath, workspace))
                throw new AbortException(Messages.HealthAnalyzerCommon_notInstalled(productName));
        }
    }

    public boolean isRegistryExistsOnSlave(@Nonnull final String registryPath, FilePath workspace) throws
            InterruptedException {
        FunctionFileCallable<String, Boolean> callable = new FunctionFileCallable<>(this::isRegistryExist,
                registryPath);
        try {
            if (OperatingSystem.isWindows())
                return workspace.act(callable);
            throw new AbortException(Messages.HealthAnalyzerCommon_registryWorksOnlyOnWindows());
        } catch (IOException e) {
            logger.log(Level.SEVERE, AN_EXCEPTION_WAS_THROWN, e);
        }
        return false;
    }

    public boolean isRegistryExist(@Nonnull String registryPath)
            throws InterruptedException, AbortException {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Callable<Boolean> task = () -> startRegistryQueryAndGetStatus(registryPath);
        Future<Boolean> future = executor.submit(task);
        executor.shutdown();
        executor.awaitTermination(500, TimeUnit.MILLISECONDS);
        return getFutureResult(future);
    }

    private boolean getFutureResult(Future<Boolean> future) throws InterruptedException, AbortException {
        try {
            return future.isDone() && future.get();
        } catch (ExecutionException e) {
            throw new AbortException("failed to get result from the thread to check if registry exist: " + e
                    .getMessage());
        }
    }

    private boolean startRegistryQueryAndGetStatus(@Nonnull final String registryPath) throws IOException {
        ProcessBuilder builder = new ProcessBuilder("reg", "query", registryPath);
        Process reg = builder.start();
        return isProcessStreamHasRegistry(reg);
    }

    private boolean isProcessStreamHasRegistry(@Nonnull final Process reg) throws IOException {
        try (BufferedReader output = new BufferedReader(
                new InputStreamReader(reg.getInputStream()))) {
            Stream<String> keys = output.lines().filter(l -> !l.isEmpty());
            return keys.findFirst().isPresent();
        }
    }

    private boolean isFileExistOnSlave(@Nonnull final String path, FilePath workspace) throws InterruptedException {
        FunctionFileCallable<String, Boolean> callable = new FunctionFileCallable<>(this::isFileExist, path);

        try {
            return workspace.act(callable);
        } catch (IOException e) {
            logger.log(Level.SEVERE, AN_EXCEPTION_WAS_THROWN, e);
        }

        return false;
    }

    public boolean isFileExist(final String path) throws AbortException {
        if (path == null || path.isEmpty())
            return true;

        File file = Paths.get(path).toFile();
        if (file.exists()) {
            if (file.isDirectory())
                throw new AbortException(Messages.HealthAnalyzerCommon_isDirectory(path));
            return file.isFile();
        }

        return false;
    }

    public void ifCheckedPerformFilesExistenceCheck(final List<VariableWrapper> files, boolean toCheck, FilePath
            workspace)
            throws AbortException, InterruptedException {
        if (!toCheck || files == null || files.isEmpty())
            return;

        for (VariableWrapper file : files)
            if (!isFileExistOnSlave(file.getField(), workspace))
                throw new AbortException(Messages.HealthAnalyzerCommon_fileNotExist(file.getField()));

    }

    public void ifCheckedPerformOsCheck(final OperatingSystem os, boolean toCheck, FilePath workspace) throws
            InterruptedException {
        SupplierFileCallable<Boolean> supplier = new SupplierFileCallable<>(os::equalsCurrentOs);

        try {
            if (toCheck && !workspace.act(supplier))
                throw new AbortException(Messages.HealthAnalyzerCommon_operatingSystemIncorrect(os.toString()
                        .toLowerCase()));
        } catch (IOException e) {
            logger.log(Level.SEVERE, AN_EXCEPTION_WAS_THROWN, e);
        }
    }
}