package com.hp.demo.e2e.rest;

import com.hp.demo.support.AbstractTest;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Functional
     com.hp.demo.e2e.rest - RestComponentTest - testNullListener
     com.hp.demo.e2e.rest - RestComponentTest - testExportTasks
     com.hp.demo.e2e.rest - RestComponentTest - testRegistrationFactory
     com.hp.demo.e2e.rest - RestComponentTest - testCreateCallback
     com.hp.demo.e2e.rest - RestComponentTest - testResetLogin
     com.hp.demo.e2e.rest - RestComponentTest - testRegistrationAccount
     com.hp.demo.e2e.rest - RestComponentTest - testRegistrationPlatform
 */
@Test(groups = { "com.hp.demo.support.FunctionalTests" })
public class RestComponentTest extends AbstractTest {
    @Test
    public void testNullListener() { Assert.assertFalse(checkHASetupProperty(), "HA Setup failed"); }

    @Test
    public void testExportTasks() { Assert.assertFalse(checkHASetupProperty(), "HA Setup failed"); }

    @Test
    public void testRegistrationFactory() { Assert.assertFalse(checkHASetupProperty(), "HA Setup failed"); }

    @Test
    public void testCreateCallback() { Assert.assertFalse(checkHASetupProperty(), "HA Setup failed"); }

    @Test
    public void testResetLogin() { Assert.assertFalse(checkHASetupProperty(), "HA Setup failed"); }

    @Test
    public void testRegistrationAccount() { Assert.assertFalse(checkHASetupProperty(), "HA Setup failed"); }

    @Test
    public void testRegistrationPlatform() { Assert.assertFalse(checkHASetupProperty(), "HA Setup failed"); }
}
