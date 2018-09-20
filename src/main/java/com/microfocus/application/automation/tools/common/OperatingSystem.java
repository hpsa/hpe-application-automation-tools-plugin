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

import java.util.Arrays;
import java.util.List;

public enum OperatingSystem {
    POSIX,
    WINDOWS,
    MAC;

    private static String systemPropertyOs;

    public static OperatingSystem get() {
        systemPropertyOs = System.getProperty("os.name").toLowerCase();

        if (isWindows())
            return WINDOWS;
        else if (isPosix())
            return POSIX;
        else if (isMac())
            return MAC;
        throw new UnsupportedOperationException(
                String.format("The operating system: %s is unsupported.", systemPropertyOs));
    }

    private static boolean isWindows() {
        return systemPropertyOs.contains(WINDOWS.name().toLowerCase());
    }

    private static boolean isMac() {
        return systemPropertyOs.contains(MAC.name().toLowerCase());
    }

    private static boolean isPosix() {
        List<String> posixNames = Arrays.asList("linux", "os/2", "irix", "hp-ux", "aix", "soalris", "sunos");
        return posixNames.contains(systemPropertyOs);
    }
}