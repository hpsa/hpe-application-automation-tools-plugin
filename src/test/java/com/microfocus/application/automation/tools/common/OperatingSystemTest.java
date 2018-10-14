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

import org.junit.*;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

public class OperatingSystemTest {
    private static String os;
    public static void initializeOperatingSystemOs(final String os) throws NoSuchFieldException, IllegalAccessException {
        changeStaticFinalField(os, "OS");

        if (os.toLowerCase().contains("windows")) {
            changeBooleanStaticFinalField(true, "IS_WINDOWS");
            changeBooleanStaticFinalField(false, "IS_MAC");
            changeBooleanStaticFinalField(false, "IS_LINUX");
        } else if (os.toLowerCase().contains("linux")) {
            changeBooleanStaticFinalField(false, "IS_WINDOWS");
            changeBooleanStaticFinalField(false, "IS_MAC");
            changeBooleanStaticFinalField(true, "IS_LINUX");
        } else if (os.toLowerCase().contains("mac")) {
            changeBooleanStaticFinalField(false, "IS_WINDOWS");
            changeBooleanStaticFinalField(true, "IS_MAC");
            changeBooleanStaticFinalField(false, "IS_LINUX");
        }
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
    public static void setup() throws Exception {
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
        initializeOperatingSystemOs("Linux");
        Assert.assertTrue(OperatingSystem.LINUX.equalsCurrentOs());
    }

    @Test
    public void equalsCurrentOs_mac() throws NoSuchFieldException, IllegalAccessException {
        initializeOperatingSystemOs("Mac OS X");
        Assert.assertTrue(OperatingSystem.MAC.equalsCurrentOs());
    }

    @Test
    public void equalsCurrentOs_invalidOsReturnsFalse() throws NoSuchFieldException, IllegalAccessException {
        initializeOperatingSystemOs("Invalid OS");
        Assert.assertFalse(OperatingSystem.WINDOWS.equalsCurrentOs());
    }
}