package com.hp.demo.e2e.image;

import com.hp.demo.support.AbstractTest;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Functional
     com.hp.demo.e2e.image - ImageUseCaseTest - testNotFoundPerformance
     com.hp.demo.e2e.image - ImageUseCaseTest - testExportModule
     com.hp.demo.e2e.image - ImageUseCaseTest - testDispatchUtils
     com.hp.demo.e2e.image - ImageUseCaseTest - testDelegateUseCase
     com.hp.demo.e2e.image - ImageUseCaseTest - testSimpleFactory
     com.hp.demo.e2e.image - ImageUseCaseTest - testResetEvent
     com.hp.demo.e2e.image - ImageUseCaseTest - testFoundHandler
     com.hp.demo.e2e.image - ImageUseCaseTest - testInvalidUtils
 */
@Test(groups = { "com.hp.demo.support.FunctionalTests" })
public class ImageUseCaseTest extends AbstractTest {
    @Test
    public void testNotFoundPerformance() { Assert.assertFalse(checkHASetupProperty(), "HA Setup failed"); }

    @Test
    public void testExportModule() { Assert.assertFalse(checkHASetupProperty(), "HA Setup failed"); }

    @Test
    public void testDispatchUtils() { Assert.assertFalse(checkHASetupProperty(), "HA Setup failed"); }

    @Test
    public void testDelegateUseCase() { Assert.assertFalse(checkHASetupProperty(), "HA Setup failed"); }

    @Test
    public void testSimpleFactory() { Assert.assertFalse(checkHASetupProperty(), "HA Setup failed"); }

    @Test
    public void testResetEvent() { Assert.assertFalse(checkHASetupProperty(), "HA Setup failed"); }

    @Test
    public void testFoundHandler() { Assert.assertFalse(checkHASetupProperty(), "HA Setup failed"); }

    @Test
    public void testInvalidUtils() { Assert.assertFalse(checkHASetupProperty(), "HA Setup failed"); }
}
