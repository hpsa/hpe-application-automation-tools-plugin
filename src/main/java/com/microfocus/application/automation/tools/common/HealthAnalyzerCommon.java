/*
 * Certain versions of software and/or documents ("Material") accessible here may contain branding from
 * Hewlett-Packard Company (now HP Inc.) and Hewlett Packard Enterprise Company.  As of September 1, 2017,
 * the Material is now offered by Micro Focus, a separately owned and operated company.  Any reference to the HP
 * and Hewlett Packard Enterprise/HPE marks is historical in nature, and the HP and Hewlett Packard Enterprise/HPE
 * marks are the property of their respective owners.
 * __________________________________________________________________
 * MIT License
 *
 * (c) Copyright 2012-2018 Micro Focus or one of its affiliates.
 *
 * The only warranties for products and services of Micro Focus and its affiliates
 * and licensors ("Micro Focus") are set forth in the express warranty statements
 * accompanying such products and services. Nothing herein should be construed as
 * constituting an additional warranty. Micro Focus shall not be liable for technical
 * or editorial errors or omissions contained herein.
 * The information contained herein is subject to change without notice.
 * ___________________________________________________________________
 */

package com.microfocus.application.automation.tools.common;

import com.microfocus.application.automation.tools.common.model.RepeatableField;
import hudson.AbortException;

import javax.annotation.Nonnull;
import javax.net.ssl.SSLHandshakeException;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.UnknownHostException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

/**
 * The HealthAnalyzerCommon provides generic methods for every product to use
 * Requires to be initiated with the product name
 */
public class HealthAnalyzerCommon {
    private final String productName;

    public HealthAnalyzerCommon(@Nonnull final String productName) {
        Objects.requireNonNull(productName, "The product name value must be not null");
        this.productName = productName;
    }

    public void ifCheckedPerformWindowsInstallationCheck(@Nonnull final String registryPath, final boolean toCheck)
            throws IOException, InterruptedException {
        Objects.requireNonNull(registryPath, "The registry value must be not null");

        if (toCheck && !isRegistryExists(registryPath))
            throwAbortException("%s is not installed, please install it first.", productName);
    }

    private boolean isRegistryExists(@Nonnull final String registryPath) throws IOException, InterruptedException {
        // TODO: Check if its windows? (System.getProperty("os.name")
        return startRegistryQueryAndGetStatus(registryPath);
    }

    private boolean startRegistryQueryAndGetStatus(@Nonnull final String registryPath)
            throws IOException, InterruptedException {
        ProcessBuilder builder = new ProcessBuilder("reg", "query", registryPath);
        Process reg = builder.start();
        boolean keys = isProcessStreamHasRegistry(reg);
        reg.waitFor(1, TimeUnit.MINUTES);
        return keys;
    }

    private boolean isProcessStreamHasRegistry(@Nonnull final Process reg) throws IOException {
        try (BufferedReader output = new BufferedReader(
                new InputStreamReader(reg.getInputStream()))) {
            Stream<String> keys = output.lines().filter(l -> !l.isEmpty());
            return keys.findFirst().isPresent();
        }
    }

    public void ifCheckedDoesUrlExist(
            @Nonnull final String url, final boolean toCheck) throws IOException {
        Objects.requireNonNull(url, "URL must be not be null");

        if (toCheck && !isURLExist(url))
            throwAbortException("The server URL of %s does not exist.", productName);
    }

    private boolean isURLExist(String stringUrl) throws IOException {
        try {
            URL url = new URL(stringUrl); // MalformedUrlException
            HttpURLConnection.setFollowRedirects(false);
            HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
            httpURLConnection.setConnectTimeout(3000); // 3 Seconds
            httpURLConnection.setRequestMethod("HEAD");
            httpURLConnection.setRequestProperty(
                    "User-Agent", "Mozilla/5.0 (Windows; U; Windows NT 6.0; en-US; rv:1.9.1.2)" +
                            " Gecko/20090729 Firefox/3.5.2 (.NET CLR 3.5.30729)");
            return httpURLConnection.getResponseCode() == HttpURLConnection.HTTP_OK;
        } catch (UnknownHostException e) {
            return false; // Todo: Provide this as information for the AbortException?
        } catch (SSLHandshakeException e) {
            return true;
        }
    }

    private void throwAbortException(@Nonnull final String message, final String... args)
            throws AbortException {
        throw new AbortException(String.format(message, (Object[]) args));
    }

    private boolean isFileExist(@Nonnull final String path) {
        File file = Paths.get(path).toFile();

        return file.exists() && !file.isDirectory() && file.isFile();
    }

    public void ifCheckedPerformFilesExistenceCheck(@Nonnull final List<RepeatableField> files, boolean toCheck)
            throws AbortException {
        if (!toCheck)
            return;

        for (RepeatableField file : files)
            if (!isFileExist(file.getField()))
                throwAbortException("The file at path: %s does not exist", file.getField());

    }

    public void ifChecekedPerformOsCheck(final OperatingSystem os, boolean toCheck) throws AbortException {
        OperatingSystem currentOs = OperatingSystem.get();

        if (toCheck && !os.equals(currentOs))
            throwAbortException("Your operating system: %s is not %s.", os.toString(), currentOs.toString());
    }
}
