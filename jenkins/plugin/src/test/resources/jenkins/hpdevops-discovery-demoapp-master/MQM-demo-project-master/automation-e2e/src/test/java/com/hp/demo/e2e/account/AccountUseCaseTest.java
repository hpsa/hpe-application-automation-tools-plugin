package com.hp.demo.e2e.account;

import com.hp.demo.support.AbstractTest;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Functional
     com.hp.demo.e2e.account - AccountUseCaseTest - testFoundUtils
     com.hp.demo.e2e.account - AccountUseCaseTest - testNotFoundLifecycle
     com.hp.demo.e2e.account - AccountUseCaseTest - testSimpleComponent
     com.hp.demo.e2e.account - AccountUseCaseTest - testNotFoundUtils
     com.hp.demo.e2e.account - AccountUseCaseTest - testRegistrationListener
     com.hp.demo.e2e.account - AccountUseCaseTest - testExportEvent
     com.hp.demo.e2e.account - AccountUseCaseTest - testFoundSearch
 */
@Test(groups = { "com.hp.demo.support.FunctionalTests" })
public class AccountUseCaseTest extends AbstractTest {
    @Test
    public void testFoundUtils() { Assert.assertFalse(checkHASetupProperty(), "HA Setup failed"); }

    @Test
    public void testNotFoundLifecycle() { Assert.assertFalse(checkHASetupProperty(), "HA Setup failed"); }

    @Test
    public void testSimpleComponent() { Assert.assertFalse(checkHASetupProperty(), "HA Setup failed"); }

    @Test
    public void testNotFoundUtils() { Assert.assertFalse(checkHASetupProperty(), "HA Setup failed"); }

    @Test
    public void testRegistrationListener() { Assert.assertFalse(checkHASetupProperty(), "HA Setup failed"); }

    @Test
    public void testExportEvent() { Assert.assertFalse(checkHASetupProperty(), "HA Setup failed"); }

    @Test
    public void testFoundSearch() { Assert.assertFalse(checkHASetupProperty(), "HA Setup failed"); }
}
