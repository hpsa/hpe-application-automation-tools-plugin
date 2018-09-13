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

import hudson.AbortException;
import org.junit.Test;

import java.io.IOException;

import static com.microfocus.application.automation.tools.common.HealthAnalyzerCommon.ifCheckedDoesUrlExist;
import static com.microfocus.application.automation.tools.common.HealthAnalyzerCommon.ifCheckedPerformWindowsInstallationCheck;
import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.fail;

public class HealthAnalyzerCommonTest {
    private final static String DUMMY_PRODUCT_NAME = "productName";
    private final static String NON_EXISTING_REGISTRY = "non\\existing\\registry\\value";

    @Test(expected = AbortException.class)
    public void isCheckedPerformWindowsInstallationCheck_throwsException_ifValueDoesNotExistsAndToCheckIsTrue()
            throws IOException, InterruptedException {
        ifCheckedPerformWindowsInstallationCheck(NON_EXISTING_REGISTRY, true, DUMMY_PRODUCT_NAME);
    }

    @Test(expected = AbortException.class)
    public void ifCheckedDoesUrlExist_throwsException_ifUrlDoesNotExistsAndToCheckIsTrue() throws IOException {
        String url = "https://non-exisiting-url-for-checking.com";
        ifCheckedDoesUrlExist(url, true, DUMMY_PRODUCT_NAME);
    }

    @Test
    public void ifCheckedDoesUrlExists_shouldNotThrowException_ifUrlExistAndToCheckIsTrue() throws IOException {
        String url = "https://www.microfocus.com/";
        try {
            ifCheckedDoesUrlExist(url, true, DUMMY_PRODUCT_NAME);
        } catch (AbortException e) {
            fail("Should not have thrown AbortException");
        }
    }

    @Test
    public void isCheckedPerformWindowsInstallationCheck_throwsCorrectExceptionValue()
            throws IOException, InterruptedException {
        try {
            ifCheckedPerformWindowsInstallationCheck(NON_EXISTING_REGISTRY, true, DUMMY_PRODUCT_NAME);
        } catch (AbortException e) {
            assertEquals(e.getMessage(), DUMMY_PRODUCT_NAME + " is not installed, please install it first.");
        }
    }

    // TODO: find registry that exists on every windows
//    @Test
//    public void isRegistryExists_shouldReturnTrue_ifValueExists() throws IOException, InterruptedException{
//        String existingRegistryValue = "HKEY_LOCAL_MACHINE\\SOFTWARE\\WOW6432Node\\Adobe\\Repair\\Acrobat Reader\\11.0\\IOD";
//        assertThat(HealthAnalyzerCommon.isRegistryExists(existingRegistryValue), is(true));
//    }
}