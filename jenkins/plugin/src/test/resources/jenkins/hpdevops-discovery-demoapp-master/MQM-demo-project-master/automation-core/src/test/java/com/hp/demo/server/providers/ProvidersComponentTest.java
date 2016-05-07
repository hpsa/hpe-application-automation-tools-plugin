package com.hp.demo.server.providers;

import com.hp.demo.support.AbstractTest;
import com.hp.demo.support.SystemTests;
import org.junit.Assert;
import org.junit.Test;
import org.junit.experimental.categories.Category;

/**
 * System
     com.hp.demo.server.providers - ProvidersComponentTest - testNotFoundProviders
     com.hp.demo.server.providers - ProvidersComponentTest - testResetCamera
     com.hp.demo.server.providers - ProvidersComponentTest - testInvalidUseCase
     com.hp.demo.server.providers - ProvidersComponentTest - testDelegateModule
     com.hp.demo.server.providers - ProvidersComponentTest - testNullLifecycle
     com.hp.demo.server.providers - ProvidersComponentTest - testResetPlatform
     com.hp.demo.server.providers - ProvidersComponentTest - testNullRest
     com.hp.demo.server.providers - ProvidersComponentTest - testCreateLogin
     com.hp.demo.server.providers - ProvidersComponentTest - testDelegateCamera
 */
@Category(SystemTests.class)
public class ProvidersComponentTest extends AbstractTest {
    @Test
    public void testNotFoundProviders() {
        Assert.assertFalse("HA Setup failed", checkHASetupProperty());
    }

    @Test
    public void testResetCamera() {
        Assert.assertFalse("HA Setup failed", checkHASetupProperty());
    }

    @Test
    public void testInvalidUseCase() {
        Assert.assertFalse("HA Setup failed", checkHASetupProperty());
    }

    @Test
    public void testDelegateModule() {
        Assert.assertFalse("HA Setup failed", checkHASetupProperty());
    }

    @Test
    public void testNullLifecycle() {
        Assert.assertFalse("HA Setup failed", checkHASetupProperty());
    }

    @Test
    public void testResetPlatform() {
        Assert.assertFalse("HA Setup failed", checkHASetupProperty());
    }

    @Test
    public void testNullRest() {
        Assert.assertFalse("HA Setup failed", checkHASetupProperty());
    }

    @Test
    public void testCreateLogin() {
        Assert.assertFalse("HA Setup failed", checkHASetupProperty());
    }

    @Test
    public void testDelegateCamera() {
        Assert.assertFalse("HA Setup failed", checkHASetupProperty());}
}
