package com.hp.demo.e2e.performance;

import com.hp.demo.support.AbstractTest;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Functional
     com.hp.demo.e2e.performance - PerformanceListenerTest - testResetFactory
     com.hp.demo.e2e.performance - PerformanceListenerTest - testCreateContact
     com.hp.demo.e2e.performance - PerformanceListenerTest - testFoundAccount
     com.hp.demo.e2e.performance - PerformanceListenerTest - testCreateFactory
 */
@Test(groups = { "com.hp.demo.support.FunctionalTests" })
public class PerformanceListenerTest extends AbstractTest {
    @Test
    public void testResetFactory() { Assert.assertFalse(checkHASetupProperty(), "HA Setup failed"); }

    @Test
    public void testCreateContact() { Assert.assertFalse(checkHASetupProperty(), "HA Setup failed"); }

    @Test
    public void testFoundAccount() { Assert.assertFalse(checkHASetupProperty(), "HA Setup failed"); }

    @Test
    public void testCreateFactory() { Assert.assertFalse(checkHASetupProperty(), "HA Setup failed"); }
}
