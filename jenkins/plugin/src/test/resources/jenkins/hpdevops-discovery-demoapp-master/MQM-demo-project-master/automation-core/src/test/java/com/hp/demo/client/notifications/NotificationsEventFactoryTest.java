package com.hp.demo.client.notifications;

import com.hp.demo.support.AbstractTest;
import com.hp.demo.support.SystemTests;
import org.junit.Assert;
import org.junit.Test;
import org.junit.experimental.categories.Category;

/**
 * System
     com.hp.demo.client.notifications - NotificationsEventFactoryTest - testSimplePerformance
     com.hp.demo.client.notifications - NotificationsEventFactoryTest - testResetSearch
     com.hp.demo.client.notifications - NotificationsEventFactoryTest - testNullListener
     com.hp.demo.client.notifications - NotificationsEventFactoryTest - testExportCamera
     com.hp.demo.client.notifications - NotificationsEventFactoryTest - testCreateModule
     com.hp.demo.client.notifications - NotificationsEventFactoryTest - testDelegateComponent
     com.hp.demo.client.notifications - NotificationsEventFactoryTest - testResetPlatform
     com.hp.demo.client.notifications - NotificationsEventFactoryTest - testInvalidPlatform
 */
@Category(SystemTests.class)
public class NotificationsEventFactoryTest extends AbstractTest {
    @Test
    public void testSimplePerformance() {
        Assert.assertFalse("HA Setup failed", checkHASetupProperty());
    }

    @Test
    public void testResetSearch() {
        Assert.assertFalse("HA Setup failed", checkHASetupProperty());
    }

    @Test
    public void testNullListener() {
        Assert.assertFalse("HA Setup failed", checkHASetupProperty());
    }

    @Test
    public void testExportCamera() {
        Assert.assertFalse("HA Setup failed", checkHASetupProperty());
    }

    @Test
    public void testCreateModule() {
        Assert.assertFalse("HA Setup failed", checkHASetupProperty());
    }

    @Test
    public void testDelegateComponent() {
        Assert.assertFalse("HA Setup failed", checkHASetupProperty());
    }

    @Test
    public void testResetPlatform() {
        Assert.assertFalse("HA Setup failed", checkHASetupProperty());
    }

    @Test
    public void testInvalidPlatform() {
        Assert.assertFalse("HA Setup failed", checkHASetupProperty());
    }
}
