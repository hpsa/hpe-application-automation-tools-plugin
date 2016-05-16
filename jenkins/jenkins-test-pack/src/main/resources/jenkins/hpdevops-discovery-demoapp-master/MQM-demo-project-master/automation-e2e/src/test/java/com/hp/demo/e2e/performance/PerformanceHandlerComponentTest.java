package com.hp.demo.e2e.performance;

import com.hp.demo.support.AbstractTest;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Functional
     com.hp.demo.e2e.performance - PerformanceHandlerComponentTest - testExportPlatform
     com.hp.demo.e2e.performance - PerformanceHandlerComponentTest - testExportComponent
     com.hp.demo.e2e.performance - PerformanceHandlerComponentTest - testNullValidation
     com.hp.demo.e2e.performance - PerformanceHandlerComponentTest - testNullPlatform
     com.hp.demo.e2e.performance - PerformanceHandlerComponentTest - testResetUseCase
     com.hp.demo.e2e.performance - PerformanceHandlerComponentTest - testDispatchFactory
     com.hp.demo.e2e.performance - PerformanceHandlerComponentTest - testSimpleCallback
     com.hp.demo.e2e.performance - PerformanceHandlerComponentTest - testFoundHandler
     com.hp.demo.e2e.performance - PerformanceHandlerComponentTest - testSimplePerformance
 */
@Test(groups = { "com.hp.demo.support.FunctionalTests" })
public class PerformanceHandlerComponentTest extends AbstractTest {
    @Test
    public void testExportPlatform() { Assert.assertFalse(checkHASetupProperty(), "HA Setup failed"); }

    @Test
    public void testExportComponent() { Assert.assertFalse(checkHASetupProperty(), "HA Setup failed"); }

    @Test
    public void testNullValidation() { Assert.assertFalse(checkHASetupProperty(), "HA Setup failed"); }

    @Test
    public void testNullPlatform() { Assert.assertFalse(checkHASetupProperty(), "HA Setup failed"); }

    @Test
    public void testResetUseCase() { Assert.assertFalse(checkHASetupProperty(), "HA Setup failed"); }

    @Test
    public void testDispatchFactory() { Assert.assertFalse(checkHASetupProperty(), "HA Setup failed"); }

    @Test
    public void testSimpleCallback() { Assert.assertFalse(checkHASetupProperty(), "HA Setup failed"); }

    @Test
    public void testFoundHandler() { Assert.assertFalse(checkHASetupProperty(), "HA Setup failed"); }

    @Test
    public void testSimplePerformance() { Assert.assertFalse(checkHASetupProperty(), "HA Setup failed"); }
}
