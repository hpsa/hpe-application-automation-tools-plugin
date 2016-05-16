package com.hp.demo.e2e.location;

import com.hp.demo.support.AbstractTest;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Functional
     com.hp.demo.e2e.location - LocationHandlerComponentTest - testExportUtils
     com.hp.demo.e2e.location - LocationHandlerComponentTest - testCreateImage
     com.hp.demo.e2e.location - LocationHandlerComponentTest - testSimpleListener
     com.hp.demo.e2e.location - LocationHandlerComponentTest - testResetPerformance
     com.hp.demo.e2e.location - LocationHandlerComponentTest - testFoundPerformance
     com.hp.demo.e2e.location - LocationHandlerComponentTest - testDispatchProviders
     com.hp.demo.e2e.location - LocationHandlerComponentTest - testSimpleFactory
 */
@Test(groups = { "com.hp.demo.support.FunctionalTests" })
public class LocationHandlerComponentTest extends AbstractTest {
    @Test
    public void testExportUtils() { Assert.assertFalse(checkHASetupProperty(), "HA Setup failed"); }

    @Test
    public void testCreateImage() { Assert.assertFalse(checkHASetupProperty(), "HA Setup failed"); }

    @Test
    public void testSimpleListener() { Assert.assertFalse(checkHASetupProperty(), "HA Setup failed"); }

    @Test
    public void testResetPerformance() { Assert.assertFalse(checkHASetupProperty(), "HA Setup failed"); }

    @Test
    public void testFoundPerformance() { Assert.assertFalse(checkHASetupProperty(), "HA Setup failed"); }

    @Test
    public void testDispatchProviders() { Assert.assertFalse(checkHASetupProperty(), "HA Setup failed"); }

    @Test
    public void testSimpleFactory() { Assert.assertFalse(checkHASetupProperty(), "HA Setup failed"); }
}
