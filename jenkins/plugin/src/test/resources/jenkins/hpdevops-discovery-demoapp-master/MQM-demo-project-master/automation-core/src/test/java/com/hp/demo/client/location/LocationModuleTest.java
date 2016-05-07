package com.hp.demo.client.location;

import com.hp.demo.support.AbstractTest;
import com.hp.demo.support.UnitTests;
import org.junit.Assert;
import org.junit.Test;
import org.junit.experimental.categories.Category;

/**
 * Unit
     com.hp.demo.client.location - LocationModuleTest - testExportUtils
     com.hp.demo.client.location - LocationModuleTest - testResetContact
     com.hp.demo.client.location - LocationModuleTest - testDelegateProviders
     com.hp.demo.client.location - LocationModuleTest - testResetFactory
     com.hp.demo.client.location - LocationModuleTest - testCreateCamera
     com.hp.demo.client.location - LocationModuleTest - testExportTasks
 */

@Category(UnitTests.class)
public class LocationModuleTest extends AbstractTest {

    @Test
    public void testExportUtils() { Assert.assertFalse("HA Setup failed", checkHASetupProperty()); }

    @Test
    public void testResetContact() { Assert.assertFalse("HA Setup failed", checkHASetupProperty()); }

    @Test
    public void testDelegateProviders() { Assert.assertFalse("HA Setup failed", checkHASetupProperty()); }

    @Test
    public void testResetFactory() { Assert.assertFalse("HA Setup failed", checkHASetupProperty()); }

    @Test
    public void testCreateCamera() { Assert.assertFalse("HA Setup failed", checkHASetupProperty()); }

    @Test
    public void testExportTasks() { Assert.assertFalse("HA Setup failed", checkHASetupProperty()); }
}
