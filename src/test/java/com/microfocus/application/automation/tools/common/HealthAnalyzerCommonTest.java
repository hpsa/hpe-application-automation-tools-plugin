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

import com.microfocus.application.automation.tools.common.utils.HealthAnalyzerCommon;
import com.microfocus.application.automation.tools.common.utils.OperatingSystem;
import hudson.AbortException;
import org.junit.After;
import org.junit.BeforeClass;
import org.junit.Test;

import static com.microfocus.application.automation.tools.common.OperatingSystemTest.initializeOperatingSystemOs;
import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.fail;

public class HealthAnalyzerCommonTest {
    private final static String DUMMY_PRODUCT_NAME = "productName";
    private final static String NON_EXISTING_REGISTRY = "non\\existing\\registry\\value";
    private static HealthAnalyzerCommon healthAnalyzerCommon;
    private static String os;

    @BeforeClass
    public static void setup() {
        healthAnalyzerCommon = new HealthAnalyzerCommon(DUMMY_PRODUCT_NAME);
        os = System.getProperty("os.name");
    }

    @After
    public void tearDown() throws Exception {
        initializeOperatingSystemOs(os);
    }

    @Test
    public void isCheckedPerformWindowsInstallationCheck_throwsException_ifValueDoesNotExistsAndToCheckIsTrue()
            throws Exception {
        boolean returnValue = healthAnalyzerCommon.isRegistryExist(NON_EXISTING_REGISTRY);
        assertEquals("Operating system mismatch", false, returnValue);
    }

    @Test
    public void runningMethodOnNonWindows_throwsException() throws Exception {
        initializeOperatingSystemOs("Linux");
        boolean returnValue = healthAnalyzerCommon.isRegistryExist(NON_EXISTING_REGISTRY);
        assertEquals("Operating system mismatch", false, returnValue);
        initializeOperatingSystemOs(System.getProperty("os.name"));
    }

    @Test
    public void runningMethodOnWindowsWhenRegistryNotExists_throwsException() throws Exception {
        if (OperatingSystem.isWindows()) {
            boolean returnValue = healthAnalyzerCommon.isRegistryExist(NON_EXISTING_REGISTRY);
            assertEquals("Operating system mismatch", false, returnValue);
        }
    }

    @Test
    public void isRegistryExists_shouldReturnTrue_ifValueExists() throws Exception {
        if (OperatingSystem.isWindows()) {
            String existingRegistryValue = "HKEY_LOCAL_MACHINE\\SOFTWARE\\Microsoft\\Windows NT\\CurrentVersion\\";
            try {
                healthAnalyzerCommon.isRegistryExist(existingRegistryValue);
            } catch (AbortException e) {
                fail("Should not have thrown AbortException");
            }
        }
    }

    @Test
    public void ifCheckedPerformFilesExistenceCheck_throwsException_ifFileDoesNotExist() throws Exception {
        String file = "C:\\non\\existing\\jenkins\\plugin\\path";
        boolean returnValue = healthAnalyzerCommon.isFileExist(file);
        assertEquals("Operating system mismatch", false, returnValue);
    }

    @Test(expected = AbortException.class)
    public void ifCheckedPerformFilesExistenceCheck_throwsException_ifDirectory() throws Exception {
        if (OperatingSystem.isWindows()) {
            String file = "C:\\Users";
            healthAnalyzerCommon.isFileExist(file);
        }
    }

    @Test
    public void ifCheckedPerformFilesExistenceCheck_notThrowing_ifFileExist() {
        String file = null;

        if (OperatingSystem.isWindows()) {
            file = "C:\\Windows\\regedit.exe";
        } else if (OperatingSystem.isMac()) {
            file = "//bin";
        } else if (OperatingSystem.isLinux()) {
            file = "//proc";
        }

        try {
            healthAnalyzerCommon.isFileExist(file);
        } catch (AbortException e) {
            fail("Should not have thrown AbortException: The file doesn't exist");
        }
    }

    @Test
    public void ifCheckedPerformFilesExistenceCheck_shouldReturnTrue_ifNoFilesExist() {
        try {
            healthAnalyzerCommon.isFileExist(null);
            String file = "";
            healthAnalyzerCommon.isFileExist(file);
        } catch (AbortException e) {
            fail("Should not have thrown AbortException");
        }
    }
}