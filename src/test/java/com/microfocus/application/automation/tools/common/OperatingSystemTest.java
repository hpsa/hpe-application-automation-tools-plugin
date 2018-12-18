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

import com.microfocus.application.automation.tools.common.utils.OperatingSystem;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

import static junit.framework.TestCase.assertEquals;

public class OperatingSystemTest {
    private static String os;

    public static void initializeOperatingSystemOs(final String os) throws NoSuchFieldException, IllegalAccessException {
        changeStaticFinalField(os, "os");

        if (os.toLowerCase().contains("windows")) {
            setAllBooleanStaticFinalFields(true, false, false);
        } else if (os.toLowerCase().contains("linux")) {
            setAllBooleanStaticFinalFields(false, false, true);
        } else if (os.toLowerCase().contains("mac")) {
            setAllBooleanStaticFinalFields(false, true, false);
        } else {
            setAllBooleanStaticFinalFields(false, false, false);
        }
    }

    private static void setAllBooleanStaticFinalFields(boolean isWindows, boolean isMac, boolean isLinux)
            throws NoSuchFieldException, IllegalAccessException {
        changeBooleanStaticFinalField(isWindows, "windows");
        changeBooleanStaticFinalField(isMac, "mac");
        changeBooleanStaticFinalField(isLinux, "linux");
    }

    private static void changeStaticFinalField(String value, String declaredField)
            throws NoSuchFieldException, IllegalAccessException {
        Field field = OperatingSystem.class.getDeclaredField(declaredField);
        field.setAccessible(true);
        Field modifiers = Field.class.getDeclaredField("modifiers");
        modifiers.setAccessible(true);
        modifiers.setInt(field, field.getModifiers() & ~Modifier.FINAL);
        field.set(null, value.toLowerCase());
    }

    private static void changeBooleanStaticFinalField(boolean value, String declaredField)
            throws NoSuchFieldException, IllegalAccessException {
        Field field = OperatingSystem.class.getDeclaredField(declaredField);
        field.setAccessible(true);
        Field modifiers = Field.class.getDeclaredField("modifiers");
        modifiers.setAccessible(true);
        modifiers.setInt(field, field.getModifiers() & ~Modifier.FINAL);
        field.set(null, value);
    }

    @BeforeClass
    public static void setup() {
        os = System.getProperty("os.name");
    }


    @AfterClass
    public static void tearDown() throws Exception {
        initializeOperatingSystemOs(os);
    }


    @Test
    public void equalsCurrentOs_windows() throws NoSuchFieldException, IllegalAccessException {
        initializeOperatingSystemOs("Windows 7");
        Assert.assertTrue(OperatingSystem.WINDOWS.equalsCurrentOs());
    }

    @Test
    public void equalsCurrentOs_linux() throws NoSuchFieldException, IllegalAccessException {
        String os = "Linux";
        initializeOperatingSystemOs(os);
        assertEquals("Operating system should be " + os, true, OperatingSystem.isLinux());
    }

    @Test
    public void equalsCurrentOs_mac() throws NoSuchFieldException, IllegalAccessException {
        String os = "Mac OS X";
        initializeOperatingSystemOs(os);
        assertEquals("Operating system should be " + os, true, OperatingSystem.isMac());
    }

    @Test
    public void equalsCurrentOs_invalidOsReturnsFalse() throws NoSuchFieldException, IllegalAccessException {
        String os = "Invalid OS";
        initializeOperatingSystemOs("Invalid OS");
        assertEquals("Operating system should be " + os, false, OperatingSystem.isWindows());
    }
}