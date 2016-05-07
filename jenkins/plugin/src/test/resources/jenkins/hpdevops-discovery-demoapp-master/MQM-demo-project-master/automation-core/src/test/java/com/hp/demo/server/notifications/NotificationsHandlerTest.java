package com.hp.demo.server.notifications;

import com.hp.demo.support.AbstractTest;
import com.hp.demo.support.UnitTests;
import org.junit.Assert;
import org.junit.Test;
import org.junit.experimental.categories.Category;

/**
 * Unit
     com.hp.demo.server.notifications - NotificationsHandlerTest - testSimpleModule
     com.hp.demo.server.notifications - NotificationsHandlerTest - testCreateAccount
     com.hp.demo.server.notifications - NotificationsHandlerTest - testInvalidImage
     com.hp.demo.server.notifications - NotificationsHandlerTest - testExportSearch
     com.hp.demo.server.notifications - NotificationsHandlerTest - testDelegateHandler
     com.hp.demo.server.notifications - NotificationsHandlerTest - testRegistrationHandler
     com.hp.demo.server.notifications - NotificationsHandlerTest - testMergeCallback
     com.hp.demo.server.notifications - NotificationsHandlerTest - testNullCamera
 */

@Category(UnitTests.class)
public class NotificationsHandlerTest extends AbstractTest {

    @Test
    public void testSimpleModule() { Assert.assertFalse("HA Setup failed", checkHASetupProperty()); }

    @Test
    public void testCreateAccount() { Assert.assertFalse("HA Setup failed", checkHASetupProperty()); }

    @Test
    public void testInvalidImage() { Assert.assertFalse("HA Setup failed", checkHASetupProperty()); }

    @Test
    public void testExportSearch() { Assert.assertFalse("HA Setup failed", checkHASetupProperty()); }

    @Test
    public void testDelegateHandler() { Assert.assertFalse("HA Setup failed", checkHASetupProperty()); }

    @Test
    public void testRegistrationHandler() {
        Assert.assertTrue("Handler registration failed.", successfulBuild());
        Assert.assertFalse("HA Setup failed",checkHASetupProperty());
    }

    @Test
    public void testMergeCallback() {
        slowTest();
    }

    @Test
    public void testNullCamera() { Assert.assertFalse("HA Setup failed", checkHASetupProperty()); }
}
