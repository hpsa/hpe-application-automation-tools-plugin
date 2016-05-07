package com.hp.demo.client.performance;

import com.hp.demo.support.AbstractTest;
import com.hp.demo.support.SystemTests;
import org.junit.Assert;
import org.junit.Test;
import org.junit.experimental.categories.Category;

/**
 * System
     com.hp.demo.client.performance - PerformanceUseCaseCallbackTest - testInvalidLocation
     com.hp.demo.client.performance - PerformanceUseCaseCallbackTest - testCreateComponent
     com.hp.demo.client.performance - PerformanceUseCaseCallbackTest - testNullFactory
     com.hp.demo.client.performance - PerformanceUseCaseCallbackTest - testResetFactory
     com.hp.demo.client.performance - PerformanceUseCaseCallbackTest - testMergeFactory
     com.hp.demo.client.performance - PerformanceUseCaseCallbackTest - testExportAccount
     com.hp.demo.client.performance - PerformanceUseCaseCallbackTest - testInvalidUtils
     com.hp.demo.client.performance - PerformanceUseCaseCallbackTest - testSimpleRest
 */
@Category(SystemTests.class)
public class PerformanceUseCaseCallbackTest extends AbstractTest {
    @Test
    public void testInvalidLocation() {
        Assert.assertFalse("HA Setup failed", checkHASetupProperty());
    }

    @Test
    public void testCreateComponent() {
        Assert.assertFalse("HA Setup failed", checkHASetupProperty());
    }

    @Test
    public void testNullFactory() {
        Assert.assertFalse("HA Setup failed", checkHASetupProperty());
    }

    @Test
    public void testResetFactory() {
        Assert.assertFalse("HA Setup failed", checkHASetupProperty());
    }

    @Test
    public void testMergeFactory() {
        Assert.assertFalse("HA Setup failed", checkHASetupProperty());
    }

    @Test
    public void testExportAccount() {
        Assert.assertFalse("HA Setup failed", checkHASetupProperty());
    }

    @Test
    public void testInvalidUtils() {
        Assert.assertFalse("HA Setup failed", checkHASetupProperty());
    }

    @Test
    public void testSimpleRest() {
        Assert.assertFalse("HA Setup failed", checkHASetupProperty());
    }
}
