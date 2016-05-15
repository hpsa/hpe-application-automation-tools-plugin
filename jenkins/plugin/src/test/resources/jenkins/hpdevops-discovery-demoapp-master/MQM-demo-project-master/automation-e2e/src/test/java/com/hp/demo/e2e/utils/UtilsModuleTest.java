package com.hp.demo.e2e.utils;

import com.hp.demo.support.AbstractTest;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Functional
     com.hp.demo.e2e.utils - UtilsModuleTest - testMergeProviders
     com.hp.demo.e2e.utils - UtilsModuleTest - testNotFoundSearch
     com.hp.demo.e2e.utils - UtilsModuleTest - testResetEvent
     com.hp.demo.e2e.utils - UtilsModuleTest - testInvalidPerformance
     com.hp.demo.e2e.utils - UtilsModuleTest - testFoundNotifications
     com.hp.demo.e2e.utils - UtilsModuleTest - testCreateLogin
     com.hp.demo.e2e.utils - UtilsModuleTest - testNotFoundLocation
     com.hp.demo.e2e.utils - UtilsModuleTest - testDispatchRest
 */
@Test(groups = { "com.hp.demo.support.FunctionalTests" })
public class UtilsModuleTest extends AbstractTest {
    @Test
    public void testMergeProviders() { Assert.assertFalse(checkHASetupProperty(), "HA Setup failed"); }

    @Test
    public void testNotFoundSearch() { Assert.assertFalse(checkHASetupProperty(), "HA Setup failed"); }

    @Test
    public void testResetEvent() { Assert.assertFalse(checkHASetupProperty(), "HA Setup failed"); }

    @Test
    public void testInvalidPerformance() { Assert.assertFalse(checkHASetupProperty(), "HA Setup failed"); }

    @Test
    public void testFoundNotifications() { Assert.assertFalse(checkHASetupProperty(), "HA Setup failed"); }

    @Test
    public void testCreateLogin() { Assert.assertFalse(checkHASetupProperty(), "HA Setup failed"); }

    @Test
    public void testNotFoundLocation() { Assert.assertFalse(checkHASetupProperty(), "HA Setup failed"); }

    @Test
    public void testDispatchRest() { Assert.assertFalse(checkHASetupProperty(), "HA Setup failed"); }
}
