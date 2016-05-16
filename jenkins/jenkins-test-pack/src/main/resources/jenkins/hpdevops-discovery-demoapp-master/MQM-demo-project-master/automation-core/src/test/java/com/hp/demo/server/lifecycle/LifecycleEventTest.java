package com.hp.demo.server.lifecycle;

import com.hp.demo.support.AbstractTest;
import com.hp.demo.support.UnitTests;
import org.junit.Assert;
import org.junit.Test;
import org.junit.experimental.categories.Category;

/**
 * Unit
     com.hp.demo.server.lifecycle - LifecycleEventTest - testNotFoundValidation
     com.hp.demo.server.lifecycle - LifecycleEventTest - testNullProviders
     com.hp.demo.server.lifecycle - LifecycleEventTest - testResetFactory
     com.hp.demo.server.lifecycle - LifecycleEventTest - testCreateAccount
     com.hp.demo.server.lifecycle - LifecycleEventTest - testInvalidListener
     com.hp.demo.server.lifecycle - LifecycleEventTest - testCreateLogin
     com.hp.demo.server.lifecycle - LifecycleEventTest - testRegistrationLifecycle
     com.hp.demo.server.lifecycle - LifecycleEventTest - testNotFoundEvent
     com.hp.demo.server.lifecycle - LifecycleEventTest - testResetProviders
 */

@Category(UnitTests.class)
public class LifecycleEventTest extends AbstractTest {

    @Test
    public void testNotFoundValidation() { Assert.assertFalse("HA Setup failed", checkHASetupProperty()); }

    @Test
    public void testNullProviders() { Assert.assertFalse("HA Setup failed", checkHASetupProperty()); }

    @Test
    public void testResetFactory() { Assert.assertFalse("HA Setup failed", checkHASetupProperty()); }

    @Test
    public void testCreateAccount() { Assert.assertFalse("HA Setup failed", checkHASetupProperty()); }

    @Test
    public void testInvalidListener() { Assert.assertFalse("HA Setup failed", checkHASetupProperty()); }

    @Test
    public void testCreateLogin() { Assert.assertFalse("HA Setup failed", checkHASetupProperty()); }

    @Test
    public void testRegistrationLifecycle() { Assert.assertFalse("HA Setup failed", checkHASetupProperty()); }

    @Test
    public void testNotFoundEvent() { Assert.assertFalse("HA Setup failed", checkHASetupProperty()); }

    @Test
    public void testResetProviders() { Assert.assertFalse("HA Setup failed", checkHASetupProperty()); }
}
