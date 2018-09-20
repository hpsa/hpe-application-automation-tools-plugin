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

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.Properties;

public class OperatingSystemTest {

    private static Properties properties;

    @BeforeClass
    public static void initialize() {
        properties = System.getProperties();
    }

    @Test
    public void get_windows() {
        properties.setProperty("os.name", "Windows 7");
        Assert.assertTrue(OperatingSystem.get().equals(OperatingSystem.WINDOWS));
    }

    @Test
    public void get_linux() {
        properties.setProperty("os.name", "Linux");
        Assert.assertTrue(OperatingSystem.get().equals(OperatingSystem.POSIX));
    }

    @Test
    public void get_mac(){
        properties.setProperty("os.name", "Mac OS X");
        Assert.assertTrue(OperatingSystem.get().equals(OperatingSystem.MAC));
    }

    @Test(expected = UnsupportedOperationException.class)
    public void get_throwsException() {
        properties.setProperty("os.name", "Invalid OS");
        OperatingSystem.get();
    }
}