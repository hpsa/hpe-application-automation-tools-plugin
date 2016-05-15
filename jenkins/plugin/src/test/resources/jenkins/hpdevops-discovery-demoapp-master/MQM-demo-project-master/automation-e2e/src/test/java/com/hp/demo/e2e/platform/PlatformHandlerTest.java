package com.hp.demo.e2e.platform;

import com.hp.demo.support.AbstractTest;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Functional
     com.hp.demo.e2e.platform - PlatformHandlerTest - testMergeLifecycle
     com.hp.demo.e2e.platform - PlatformHandlerTest - testDelegateRest
     com.hp.demo.e2e.platform - PlatformHandlerTest - testNullTasks
     com.hp.demo.e2e.platform - PlatformHandlerTest - testRegistrationLocation
     com.hp.demo.e2e.platform - PlatformHandlerTest - testRegistrationTasks
     com.hp.demo.e2e.platform - PlatformHandlerTest - testRegistrationPlatform
     com.hp.demo.e2e.platform - PlatformHandlerTest - testRegistrationProviders
 */
@Test(groups = { "com.hp.demo.support.FunctionalTests" })
public class PlatformHandlerTest extends AbstractTest {
    @Test
    public void testMergeLifecycle() { Assert.assertFalse(checkHASetupProperty(), "HA Setup failed"); }

    @Test
    public void testDelegateRest() { Assert.assertFalse(checkHASetupProperty(), "HA Setup failed"); }

    @Test
    public void testNullTasks() { Assert.assertFalse(checkHASetupProperty(), "HA Setup failed"); }

    @Test
    public void testRegistrationLocation() { Assert.assertFalse(checkHASetupProperty(), "HA Setup failed"); }

    @Test
    public void testRegistrationTasks() { Assert.assertFalse(checkHASetupProperty(), "HA Setup failed"); }

    @Test
    public void testRegistrationPlatform() { Assert.assertFalse(checkHASetupProperty(), "HA Setup failed"); }

    @Test
    public void testRegistrationProviders() { Assert.assertFalse(checkHASetupProperty(), "HA Setup failed"); }
}
