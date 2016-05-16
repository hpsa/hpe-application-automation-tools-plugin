package com.hp.demo.server.location;

import com.hp.demo.support.AbstractTest;
import com.hp.demo.support.SystemTests;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.experimental.categories.Category;

/**
 * System
     com.hp.demo.server.location - LocationModuleTest - testNotFoundSearch
     com.hp.demo.server.location - LocationModuleTest - testFoundSearch
     com.hp.demo.server.location - LocationModuleTest - testResetCamera
     com.hp.demo.server.location - LocationModuleTest - testMergeModule
     com.hp.demo.server.location - LocationModuleTest - testDispatchLogin
     com.hp.demo.server.location - LocationModuleTest - testCreateRest
     com.hp.demo.server.location - LocationModuleTest - testDispatchUtils
     com.hp.demo.server.location - LocationModuleTest - testDispatchSearch
     com.hp.demo.server.location - LocationModuleTest - testExportComponent
 */
@Category(SystemTests.class)
public class LocationModuleTest extends AbstractTest {
    @Test
    @Ignore
    public void testNotFoundSearch() { Assert.assertFalse("HA Setup failed", checkHASetupProperty()); }

    @Test
    public void testFoundSearch() {
        Assert.assertFalse("HA Setup failed", checkHASetupProperty());
    }

    @Test
    public void testResetCamera() {
        Assert.assertFalse("HA Setup failed", checkHASetupProperty());
    }

    @Test
    public void testMergeModule() {
        Assert.assertFalse("HA Setup failed", checkHASetupProperty());
    }

    @Test
    public void testDispatchLogin() {
        Assert.assertFalse("HA Setup failed", checkHASetupProperty());
    }

    @Test
    public void testCreateRest() {
        Assert.assertFalse("HA Setup failed", checkHASetupProperty());
    }

    @Test
    public void testDispatchUtils() {
        Assert.assertFalse("HA Setup failed", checkHASetupProperty());
    }

    @Test
    public void testDispatchSearch() {
        Assert.assertFalse("HA Setup failed", checkHASetupProperty());
    }

    @Test
    public void testExportComponent() {
        Assert.assertFalse("HA Setup failed", checkHASetupProperty());
    }
}
