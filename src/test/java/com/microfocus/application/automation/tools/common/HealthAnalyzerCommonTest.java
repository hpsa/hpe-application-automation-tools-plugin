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

import com.microfocus.application.automation.tools.common.model.VariableWrapper;
import com.microfocus.application.automation.tools.common.utils.HealthAnalyzerCommon;
import com.microfocus.application.automation.tools.common.utils.OperatingSystem;
import hudson.AbortException;
import org.junit.After;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

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

    @Test(expected = AbortException.class)
    public void isCheckedPerformWindowsInstallationCheck_throwsException_ifValueDoesNotExistsAndToCheckIsTrue()
            throws Exception {
        healthAnalyzerCommon.ifCheckedPerformWindowsInstallationCheck(NON_EXISTING_REGISTRY, true);
    }

    @Test
    public void isCheckedPerformWindowsInstallationCheck_throwsCorrectExceptionValue() throws Exception {
        if (OperatingSystem.IS_WINDOWS) {
            try {
                healthAnalyzerCommon.ifCheckedPerformWindowsInstallationCheck(NON_EXISTING_REGISTRY, true);
                fail();
            } catch (AbortException e) {
                assertEquals(e.getMessage(), DUMMY_PRODUCT_NAME + " is not installed. Please install it first.");
            }
        }
    }

    @Test(expected = AbortException.class)
    public void runningMethodOnNonWindows_throwsException() throws Exception {
        initializeOperatingSystemOs("Linux");
        healthAnalyzerCommon.ifCheckedPerformWindowsInstallationCheck(NON_EXISTING_REGISTRY, true);
        initializeOperatingSystemOs(System.getProperty("os.name"));
    }

    @Test(expected = AbortException.class)
    public void runningMethodOnWindowsWhenRegistryNotExists_throwsException() throws Exception {
        if (OperatingSystem.IS_WINDOWS) {
            healthAnalyzerCommon.ifCheckedPerformWindowsInstallationCheck(NON_EXISTING_REGISTRY, true);
        }
    }

    @Test
    public void isRegistryExists_shouldReturnTrue_ifValueExists() throws Exception {
        if (OperatingSystem.IS_WINDOWS) {
            String existingRegistryValue = "HKEY_LOCAL_MACHINE\\SOFTWARE\\Microsoft\\Windows NT\\CurrentVersion\\";
            try {
                healthAnalyzerCommon.ifCheckedPerformWindowsInstallationCheck(existingRegistryValue, true);
            } catch (AbortException e) {
                fail("Should not have thrown AbortException");
            }
        }
    }

    @Test(expected = AbortException.class)
    public void ifCheckedPerformFilesExistenceCheck_throwsException_ifFileDoesNotExist() throws Exception {
        List<VariableWrapper> files = new ArrayList<>();
        VariableWrapper field = new VariableWrapper("C:\\non\\existing\\jenkins\\plugin\\path");
        files.add(field);
        healthAnalyzerCommon.ifCheckedPerformFilesExistenceCheck(files, true, workspace);
    }

    @Test(expected = AbortException.class)
    public void ifCheckedPerformFilesExistenceCheck_throwsException_ifDirectory() throws Exception {
        if (OperatingSystem.IS_WINDOWS) {
            List<VariableWrapper> files = new ArrayList<>();
            VariableWrapper field = new VariableWrapper("C:\\Users");
            files.add(field);
            healthAnalyzerCommon.ifCheckedPerformFilesExistenceCheck(files, true, workspace);
        }
    }

    @Test
    public void ifCheckedPerformFilesExistenceCheck_notThrowing_ifFileExist() {
        List<VariableWrapper> files = new ArrayList<>();
        VariableWrapper field = null;

        if (OperatingSystem.IS_WINDOWS) {
            field = new VariableWrapper("C:\\Windows\\regedit.exe");
        } else if (OperatingSystem.IS_MAC) {
            // TODO
        } else if (OperatingSystem.IS_LINUX) {
            field = new VariableWrapper("//proc");
        }

        files.add(field);
        try {
            healthAnalyzerCommon.ifCheckedPerformFilesExistenceCheck(files, true, workspace);
        } catch (AbortException e) {
            fail("Should not have thrown AbortException: The file doesn't exist");
        }
    }

    @Test
    public void ifCheckedPerformFilesExistenceCheck_shouldReturnTrue_ifNoFilesExist() {
        try {
            healthAnalyzerCommon.ifCheckedPerformFilesExistenceCheck(null, true, workspace);
            List<VariableWrapper> files = new ArrayList<>();
            healthAnalyzerCommon.ifCheckedPerformFilesExistenceCheck(files, true, workspace);
        } catch (AbortException e) {
            fail("Should not have thrown AbortException");
        }
    }

    @Test
    public void ifCheckedPerformOsCheck_shouldThrowException_withCorrectValue() throws Exception {
        initializeOperatingSystemOs("windows");
        try {
            healthAnalyzerCommon.ifCheckedPerformOsCheck(OperatingSystem.LINUX, true);
            fail();
        } catch (AbortException e) {
            assertEquals(e.getMessage(), String.format("Your operating system: %s is not %s.", "linux", OperatingSystem.getOs()));
        }
    }

    @Test
    public void ifCheckedPerformOsCheck_doesNotThrowException() throws Exception {
        initializeOperatingSystemOs("windows");
        healthAnalyzerCommon.ifCheckedPerformOsCheck(OperatingSystem.WINDOWS, true);
    }
}