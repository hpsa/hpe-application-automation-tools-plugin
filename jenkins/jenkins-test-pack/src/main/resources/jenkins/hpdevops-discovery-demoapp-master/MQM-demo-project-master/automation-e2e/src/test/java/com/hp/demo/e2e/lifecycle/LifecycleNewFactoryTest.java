package com.hp.demo.e2e.lifecycle;

import com.hp.demo.support.AbstractTest;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Functional
     com.hp.demo.e2e.lifecycle - LifecycleFactoryTest - testNullLocation
     com.hp.demo.e2e.lifecycle - LifecycleFactoryTest - testSimpleCallback
     com.hp.demo.e2e.lifecycle - LifecycleFactoryTest - testResetLocation
     com.hp.demo.e2e.lifecycle - LifecycleFactoryTest - testMergeValidation
 */
@Test(groups = { "com.hp.demo.support.FunctionalTests" })
public class LifecycleNewFactoryTest extends AbstractTest {
    @Test
    public void testNullLocation() { Assert.assertFalse(checkHASetupProperty(), "HA Setup failed"); }

    @Test
    public void testSimpleCallback() { Assert.assertFalse(checkHASetupProperty(), "HA Setup failed"); }

    @Test
    public void testResetLocation() { Assert.assertFalse(checkHASetupProperty(), "HA Setup failed"); }

    @Test
    public void testMergeValidation() { Assert.assertFalse(checkHASetupProperty(), "HA Setup failed"); }
}
