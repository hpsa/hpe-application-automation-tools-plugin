package com.hp.demo.e2e.search;

import com.hp.demo.support.AbstractTest;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Functional
     com.hp.demo.e2e.search - SearchNegativeTest - testDelegateComponent
     com.hp.demo.e2e.search - SearchNegativeTest - testRegistrationModule
     com.hp.demo.e2e.search - SearchNegativeTest - testDelegateModule
     com.hp.demo.e2e.search - SearchNegativeTest - testRegistrationUtils
     com.hp.demo.e2e.search - SearchNegativeTest - testCreateNotifications
     com.hp.demo.e2e.search - SearchNegativeTest - testResetLogin
     com.hp.demo.e2e.search - SearchNegativeTest - testRegistrationLogin
     com.hp.demo.e2e.search - SearchNegativeTest - testRegistrationTasks
     com.hp.demo.e2e.search - SearchNegativeTest - testSimpleNegative
 */
@Test(groups = { "com.hp.demo.support.FunctionalTests" })
public class SearchNegativeTest extends AbstractTest {
    @Test
    public void testDelegateComponent() { Assert.assertFalse(checkHASetupProperty(), "HA Setup failed"); }

    @Test
    public void testRegistrationModule() { Assert.assertFalse(checkHASetupProperty(), "HA Setup failed"); }

    @Test
    public void testDelegateModule() { Assert.assertFalse(checkHASetupProperty(), "HA Setup failed"); }

    @Test
    public void testRegistrationUtils() { Assert.assertFalse(checkHASetupProperty(), "HA Setup failed"); }

    @Test
    public void testCreateNotifications() { Assert.assertFalse(checkHASetupProperty(), "HA Setup failed"); }

    @Test
    public void testResetLogin() { Assert.assertFalse(checkHASetupProperty(), "HA Setup failed"); }

    @Test
    public void testRegistrationLogin() { Assert.assertFalse(checkHASetupProperty(), "HA Setup failed"); }

    @Test
    public void testRegistrationTasks() { Assert.assertFalse(checkHASetupProperty(), "HA Setup failed"); }

    @Test
    public void testSimpleNegative() { Assert.assertFalse(checkHASetupProperty(), "HA Setup failed"); }
}
