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

import org.junit.Test;

import java.io.IOException;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.*;

public class HealthAnalyzerCommonTest {

    @Test
    public void isRegistryExists_shouldReturnFalse_ifValueDoesNotExists() throws IOException, InterruptedException {
        String falseRegistryValue = "non\\existing\\registry\\value";
        assertThat(HealthAnalyzerCommon.isRegistryExists(falseRegistryValue), is(false));
    }

    // TODO: find registry that exists on every windows
//    @Test
//    public void isRegistryExists_shouldReturnTrue_ifValueExists() throws IOException, InterruptedException{
//        String existingRegistryValue = "HKEY_LOCAL_MACHINE\\SOFTWARE\\WOW6432Node\\Adobe\\Repair\\Acrobat Reader\\11.0\\IOD";
//        assertThat(HealthAnalyzerCommon.isRegistryExists(existingRegistryValue), is(true));
//    }
}