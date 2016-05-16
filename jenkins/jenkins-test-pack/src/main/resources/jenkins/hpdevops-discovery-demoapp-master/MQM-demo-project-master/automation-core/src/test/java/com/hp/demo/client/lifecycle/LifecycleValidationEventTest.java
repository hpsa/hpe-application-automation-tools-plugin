package com.hp.demo.client.lifecycle;

import com.hp.demo.support.AbstractTest;
import com.hp.demo.support.UnitTests;
import org.junit.Assert;
import org.junit.Test;
import org.junit.experimental.categories.Category;

/**
 * Unit
     com.hp.demo.client.lifecycle - LifecycleValidationEventTest - testFoundHandler
     com.hp.demo.client.lifecycle - LifecycleValidationEventTest - testInvalidHandler
     com.hp.demo.client.lifecycle - LifecycleValidationEventTest - testSimpleCamera
     com.hp.demo.client.lifecycle - LifecycleValidationEventTest - testFoundCallback
     com.hp.demo.client.lifecycle - LifecycleValidationEventTest - testRegistrationComponent
     com.hp.demo.client.lifecycle - LifecycleValidationEventTest - testMergeNegative
     com.hp.demo.client.lifecycle - LifecycleValidationEventTest - testDispatchCamera
     com.hp.demo.client.lifecycle - LifecycleValidationEventTest - testNullValidation
 */

@Category(UnitTests.class)
public class LifecycleValidationEventTest extends AbstractTest {

    @Test
    public void testFoundHandler() { Assert.assertFalse("HA Setup failed", checkHASetupProperty()); }

    @Test
    public void testInvalidHandler() { Assert.assertFalse("HA Setup failed", checkHASetupProperty()); }

    @Test
    public void testSimpleCamera() { Assert.assertFalse("HA Setup failed", checkHASetupProperty()); }

    @Test
    public void testFoundCallback() { Assert.assertFalse("HA Setup failed", checkHASetupProperty()); }

    @Test
    public void testRegistrationComponent() { Assert.assertFalse("HA Setup failed", checkHASetupProperty()); }

    @Test
    public void testMergeNegative() { Assert.assertFalse("HA Setup failed", checkHASetupProperty()); }

    @Test
    public void testDispatchCamera() { Assert.assertFalse("HA Setup failed", checkHASetupProperty()); }

    @Test
    public void testNullValidation() { Assert.assertFalse("HA Setup failed", checkHASetupProperty()); }
}
