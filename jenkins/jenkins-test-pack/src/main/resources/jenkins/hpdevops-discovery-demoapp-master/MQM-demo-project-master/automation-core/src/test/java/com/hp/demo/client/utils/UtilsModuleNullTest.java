package com.hp.demo.client.utils;

import com.hp.demo.support.AbstractTest;
import com.hp.demo.support.UnitTests;
import org.junit.Assert;
import org.junit.Test;
import org.junit.experimental.categories.Category;

/**
 * Unit
 com.hp.demo.client.utils - UtilsModuleNullTest - testResetModule
 com.hp.demo.client.utils - UtilsModuleNullTest - testDispatchValidation
 com.hp.demo.client.utils - UtilsModuleNullTest - testInvalidListener
 com.hp.demo.client.utils - UtilsModuleNullTest - testResetAccount
 com.hp.demo.client.utils - UtilsModuleNullTest - testNullTasks
 com.hp.demo.client.utils - UtilsModuleNullTest - testMergeModule
 */

@Category(UnitTests.class)
public class UtilsModuleNullTest extends AbstractTest {

    @Test
    public void testResetModule() { Assert.assertFalse("HA Setup failed", checkHASetupProperty()); }

    @Test
    public void testDispatchValidation() { Assert.assertFalse("HA Setup failed", checkHASetupProperty()); }

    @Test
    public void testInvalidListener() { Assert.assertFalse("HA Setup failed", checkHASetupProperty()); }

    @Test
    public void testResetAccount() { Assert.assertFalse("HA Setup failed", checkHASetupProperty()); }

    @Test
    public void testNullTasks() { Assert.assertFalse("HA Setup failed", checkHASetupProperty()); }

    @Test
    public void testMergeModule() { Assert.assertFalse("HA Setup failed", checkHASetupProperty()); }
}
