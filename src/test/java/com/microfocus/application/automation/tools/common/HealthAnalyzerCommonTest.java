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
import hudson.AbortException;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.fail;

public class HealthAnalyzerCommonTest {
    private final static String DUMMY_PRODUCT_NAME = "productName";
    private final static String NON_EXISTING_REGISTRY = "non\\existing\\registry\\value";
    private static HealthAnalyzerCommon healthAnalyzerCommon;

    @BeforeClass
    public static void setup() {
        healthAnalyzerCommon = new HealthAnalyzerCommon(DUMMY_PRODUCT_NAME);
    }

    @Test(expected = AbortException.class)
    public void isCheckedPerformWindowsInstallationCheck_throwsException_ifValueDoesNotExistsAndToCheckIsTrue()
            throws IOException, InterruptedException {
        healthAnalyzerCommon.ifCheckedPerformWindowsInstallationCheck(NON_EXISTING_REGISTRY, true);
    }

    @Test(expected = AbortException.class)
    public void ifCheckedIsUrlExist_throwsException_ifUrlDoesNotExistsAndToCheckIsTrue() throws IOException {
        String url = "https://non-exisiting-url-for-checking.com";
        healthAnalyzerCommon.ifCheckedDoesUrlExist(url, true);
    }

    @Test
    public void ifCheckedIsUrlExists_shouldNotThrowException_ifUrlExistAndToCheckIsTrue() throws IOException {
        String url = "https://www.microfocus.com/";
        try {
            healthAnalyzerCommon.ifCheckedDoesUrlExist(url, true);
        } catch (AbortException e) {
            fail("Should not have thrown AbortException");
        }
    }

    @Test
    public void isCheckedPerformWindowsInstallationCheck_throwsCorrectExceptionValue()
            throws IOException, InterruptedException {
        if (OperatingSystem.WINDOWS.equalsCurrentOs()) {
            try {
                healthAnalyzerCommon.ifCheckedPerformWindowsInstallationCheck(NON_EXISTING_REGISTRY, true);
            } catch (AbortException e) {
                assertEquals(e.getMessage(), DUMMY_PRODUCT_NAME + " is not installed, please install it first.");
            }
        }
    }

    @Test
    public void isRegistryExists_shouldReturnTrue_ifValueExists() throws IOException, InterruptedException {
        if (OperatingSystem.WINDOWS.equalsCurrentOs()) {
            String existingRegistryValue = "HKEY_LOCAL_MACHINE\\SOFTWARE\\Microsoft\\Windows NT\\CurrentVersion\\";
            try {
                healthAnalyzerCommon.ifCheckedPerformWindowsInstallationCheck(existingRegistryValue, true);
            } catch (AbortException e) {
                fail("Should not have thrown AbortException");
            }
        }
    }

    @Test(expected = AbortException.class)
    public void ifCheckedPerformFilesExistenceCheck_throwsException_ifFileDoesNotExist() throws AbortException {
        List<VariableWrapper> files = new ArrayList<>();
        VariableWrapper field = new VariableWrapper("C:\\non\\existing\\jenkins\\plugin\\path");
        files.add(field);
        healthAnalyzerCommon.ifCheckedPerformFilesExistenceCheck(files, true);
    }

    @Test(expected = AbortException.class)
    public void ifCheckedPerformFilesExistenceCheck_throwsException_ifDirectory() throws AbortException {
        if (OperatingSystem.WINDOWS.equalsCurrentOs()) {
            List<VariableWrapper> files = new ArrayList<>();
            VariableWrapper field = new VariableWrapper("C:\\Users");
            files.add(field);
            healthAnalyzerCommon.ifCheckedPerformFilesExistenceCheck(files, true);
        }
    }

    @Test
    public void ifCheckedPerformFilesExistenceCheck_notThrowing_ifFileExist() {
        List<VariableWrapper> files = new ArrayList<>();
        VariableWrapper field = null;

        if (OperatingSystem.WINDOWS.equalsCurrentOs()) {
            field = new VariableWrapper("C:\\Windows\\notepad.exe");
        } else if (OperatingSystem.MAC.equalsCurrentOs()) {
            // TODO
        } else if (OperatingSystem.LINUX.equalsCurrentOs()) {
            field = new VariableWrapper("//proc");
        }

        files.add(field);
        try {
            healthAnalyzerCommon.ifCheckedPerformFilesExistenceCheck(files, true);
        } catch (AbortException e) {
            fail("Should not have thrown AbortException");
        }
    }

    @Test
    public void ifCheckedPerformFilesExistenceCheck_shouldReturnTrue_ifNoFilesExist() {

        try {
            healthAnalyzerCommon.ifCheckedPerformFilesExistenceCheck(null, true);
            List<VariableWrapper> files = new ArrayList<>();
            healthAnalyzerCommon.ifCheckedPerformFilesExistenceCheck(files, true);
        } catch (AbortException e) {
            fail("Should not have thrown AbortException");
        }
    }
}