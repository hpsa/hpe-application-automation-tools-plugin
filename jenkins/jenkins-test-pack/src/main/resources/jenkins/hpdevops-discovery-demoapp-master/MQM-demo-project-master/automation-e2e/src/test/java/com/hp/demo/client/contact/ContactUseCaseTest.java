package com.hp.demo.client.contact;

import com.hp.demo.support.AbstractTest;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * REST
     com.hp.demo.client.contact - ContactUseCaseTest - testFoundPerformance
     com.hp.demo.client.contact - ContactUseCaseTest - testDispatchUseCase
     com.hp.demo.client.contact - ContactUseCaseTest - testInvalidListener
     com.hp.demo.client.contact - ContactUseCaseTest - testMergeSearch
     com.hp.demo.client.contact - ContactUseCaseTest - testCreateLifecycle
     com.hp.demo.client.contact - ContactUseCaseTest - testNullProviders
     com.hp.demo.client.contact - ContactUseCaseTest - testExportUtils
     com.hp.demo.client.contact - ContactUseCaseTest - testDelegateHandler
 */
@Test(groups = "com.hp.demo.support.RESTTests")
public class ContactUseCaseTest extends AbstractTest {
    @Test
    public void testFoundPerformance() {
        Assert.assertFalse(checkHASetupProperty(), "HA Setup failed");
    }

    @Test
    public void testDispatchUseCase() {
        Assert.assertFalse(checkHASetupProperty(), "HA Setup failed");
    }

    @Test
    public void testInvalidListener() {
        Assert.assertFalse(checkHASetupProperty(), "HA Setup failed");
    }

    @Test
    public void testMergeSearch() {
        Assert.assertFalse(checkHASetupProperty(), "HA Setup failed");
    }

    @Test
    public void testCreateLifecycle() {
        Assert.assertFalse(checkHASetupProperty(), "HA Setup failed");
    }

    @Test
    public void testNullProviders() {
        Assert.assertFalse(checkHASetupProperty(), "HA Setup failed");
    }

    @Test
    public void testExportUtils() {
        Assert.assertFalse(checkHASetupProperty(), "HA Setup failed");
    }

    @Test
    public void testDelegateHandler() {
        Assert.assertFalse(checkHASetupProperty(), "HA Setup failed");
    }
}
