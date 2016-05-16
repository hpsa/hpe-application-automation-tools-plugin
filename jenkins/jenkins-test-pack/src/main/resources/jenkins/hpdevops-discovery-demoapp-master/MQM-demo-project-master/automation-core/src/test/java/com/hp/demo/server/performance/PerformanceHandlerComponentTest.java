package com.hp.demo.server.performance;

import com.hp.demo.support.AbstractTest;
import com.hp.demo.support.SystemTests;
import org.junit.Assert;
import org.junit.Test;
import org.junit.experimental.categories.Category;

/**
 * System
     com.hp.demo.server.performance - PerformanceHandlerComponentTest - testDispatchRest
     com.hp.demo.server.performance - PerformanceHandlerComponentTest - testResetSearch
     com.hp.demo.server.performance - PerformanceHandlerComponentTest - testMergeNegative
     com.hp.demo.server.performance - PerformanceHandlerComponentTest - testResetAccount
     com.hp.demo.server.performance - PerformanceHandlerComponentTest - testSimpleLifecycle
     com.hp.demo.server.performance - PerformanceHandlerComponentTest - testResetFactory
 */
@Category(SystemTests.class)
public class PerformanceHandlerComponentTest extends AbstractTest {
    @Test
    public void testDispatchRest() {
        Assert.assertFalse("HA Setup failed", checkHASetupProperty());
    }

    @Test
    public void testResetSearch() {
        Assert.assertFalse("HA Setup failed", checkHASetupProperty());
    }

    @Test
    public void testMergeNegative() {
        Assert.assertFalse("HA Setup failed", checkHASetupProperty());
    }

    @Test
    public void testResetAccount() {
        Assert.assertFalse("HA Setup failed", checkHASetupProperty());
    }

    @Test
    public void testSimpleLifecycle() {
        Assert.assertFalse("HA Setup failed", checkHASetupProperty());
    }

    @Test
    public void testResetFactory() {
        Assert.assertFalse("HA Setup failed", checkHASetupProperty());
    }
}
