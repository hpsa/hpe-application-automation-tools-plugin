package com.hp.demo.server.platform;

import com.hp.demo.support.AbstractTest;
import com.hp.demo.support.SystemTests;
import org.junit.Assert;
import org.junit.Test;
import org.junit.experimental.categories.Category;

/**
 * System
     com.hp.demo.server.platform - PlatformHandlerTest - testNotFoundComponent
     com.hp.demo.server.platform - PlatformHandlerTest - testInvalidFactory
     com.hp.demo.server.platform - PlatformHandlerTest - testCreateCallback
     com.hp.demo.server.platform - PlatformHandlerTest - testMergeValidation
     com.hp.demo.server.platform - PlatformHandlerTest - testSimpleUtils
     com.hp.demo.server.platform - PlatformHandlerTest - testSimpleListener
     com.hp.demo.server.platform - PlatformHandlerTest - testMergeTasks
 */
@Category(SystemTests.class)
public class PlatformHandlerTest extends AbstractTest {
    @Test
    public void testNotFoundComponent() {
        Assert.assertFalse("HA Setup failed", checkHASetupProperty());
    }

    @Test
    public void testInvalidFactory() {
        Assert.assertFalse("HA Setup failed", checkHASetupProperty());
    }

    @Test
    public void testCreateCallback() {
        Assert.assertFalse("HA Setup failed", checkHASetupProperty());
    }

    @Test
    public void testMergeValidation() {
        Assert.assertFalse("HA Setup failed", checkHASetupProperty());
    }

    @Test
    public void testSimpleUtils() {
        Assert.assertFalse("HA Setup failed", checkHASetupProperty());
    }

    @Test
    public void testSimpleListener() {
        Assert.assertFalse("HA Setup failed", checkHASetupProperty());
    }

    @Test
    public void testMergeTasks() {
        Assert.assertFalse("HA Setup failed", checkHASetupProperty());
    }
}
