package com.hp.demo.e2e.lifecycle;

import com.hp.demo.support.AbstractTest;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Functional
     com.hp.demo.e2e.lifecycle - LifecycleCallbackTest - testDelegateImage
     com.hp.demo.e2e.lifecycle - LifecycleCallbackTest - testResetSearch
     com.hp.demo.e2e.lifecycle - LifecycleCallbackTest - testRegistrationValidation
     com.hp.demo.e2e.lifecycle - LifecycleCallbackTest - testDelegateTasks
     com.hp.demo.e2e.lifecycle - LifecycleCallbackTest - testDelegateNotifications
     com.hp.demo.e2e.lifecycle - LifecycleCallbackTest - testExportFactory
 */
@Test(groups = { "com.hp.demo.support.FunctionalTests" })
public class LifecycleCallbackTest extends AbstractTest {
    @Test
    public void testDelegateImage() { Assert.assertFalse(checkHASetupProperty(), "HA Setup failed"); }

    @Test
    public void testResetSearch() { Assert.assertFalse(checkHASetupProperty(), "HA Setup failed"); }

    @Test
    public void testRegistrationValidation() { Assert.assertFalse(checkHASetupProperty(), "HA Setup failed"); }

    @Test
    public void testDelegateTasks() { Assert.assertFalse(checkHASetupProperty(), "HA Setup failed"); }

    @Test
    public void testDelegateNotifications() { Assert.assertFalse(checkHASetupProperty(), "HA Setup failed"); }

    @Test
    public void testExportFactory() { Assert.assertFalse(checkHASetupProperty(), "HA Setup failed"); }
}
