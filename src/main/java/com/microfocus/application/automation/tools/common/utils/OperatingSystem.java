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