package com.hp.demo.e2e.lifecycle;

import com.hp.demo.support.AbstractTest;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Functional
     com.hp.demo.e2e.lifecycle - LifecycleFactoryTest - testExportContact
     com.hp.demo.e2e.lifecycle - LifecycleFactoryTest - testNotFoundUseCase
     com.hp.demo.e2e.lifecycle - LifecycleFactoryTest - testNotFoundNegative
     com.hp.demo.e2e.lifecycle - LifecycleFactoryTest - testResetModule
 */
@Test(groups = { "com.hp.demo.support.FunctionalTests" })
public class LifecycleFactoryTest extends AbstractTest {
    @Test
    public void testExportContact() { Assert.assertFalse(checkHASetupProperty(), "HA Setup failed"); }

    @Test
    public void testNotFoundUseCase() { Assert.assertFalse(checkHASetupProperty(), "HA Setup failed"); }

    @Test
    public void testNotFoundNegative() { Assert.assertFalse(checkHASetupProperty(), "HA Setup failed"); }

    @Test
    public void testResetModule() { Assert.assertFalse(checkHASetupProperty(), "HA Setup failed"); }
}
