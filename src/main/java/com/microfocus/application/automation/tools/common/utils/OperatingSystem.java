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

package com.microfocus.application.automation.tools.common.utils;

import java.util.Arrays;
import java.util.List;

public enum OperatingSystem {
    LINUX,
    WINDOWS,
    MAC;

    private static final List<String> POSIX_NAMES = Arrays.asList(
            "linux", "os/2", "irix", "hp-ux", "aix", "soalris", "sunos");
    private static String os = System.getProperty("os.name").toLowerCase();
    private static boolean windows = os.contains(WINDOWS.name().toLowerCase());
    private static boolean mac = os.contains(MAC.name().toLowerCase());
    private static boolean linux = POSIX_NAMES.contains(os.toLowerCase());

    public static String getOs() {
        return os;
    }

    private static void refreshOsVariablesForSlave() {
        os = System.getProperty("os.name").toLowerCase();
        windows = os.contains(WINDOWS.name().toLowerCase());
        mac = os.contains(MAC.name().toLowerCase());
        linux = POSIX_NAMES.contains(os.toLowerCase());
    }

    public boolean equalsCurrentOs() {
        refreshOsVariablesForSlave();
        return linux || os.contains(this.name().toLowerCase());
    }

    public static boolean isWindows() {
        return windows;
    }

    public static boolean isMac() {
        return mac;
    }

    public static boolean isLinux() {
        return linux;
    }
}