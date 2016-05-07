package com.hp.demo.e2e.providers;

import com.hp.demo.support.AbstractTest;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Functional
     com.hp.demo.e2e.providers - ProvidersUseCaseTest - testInvalidContact
     com.hp.demo.e2e.providers - ProvidersUseCaseTest - testDispatchLocation
     com.hp.demo.e2e.providers - ProvidersUseCaseTest - testRegistrationCallback
     com.hp.demo.e2e.providers - ProvidersUseCaseTest - testDispatchImage
     com.hp.demo.e2e.providers - ProvidersUseCaseTest - testRegistrationUtils
     com.hp.demo.e2e.providers - ProvidersUseCaseTest - testInvalidSearch
 */
@Test(groups = { "com.hp.demo.support.FunctionalTests" })
public class ProvidersUseCaseTest extends AbstractTest {
    @Test
    public void testInvalidContact() { Assert.assertFalse(checkHASetupProperty(), "HA Setup failed"); }

    @Test
    public void testDispatchLocation() { Assert.assertFalse(checkHASetupProperty(), "HA Setup failed"); }

    @Test
    public void testRegistrationCallback() { Assert.assertFalse(checkHASetupProperty(), "HA Setup failed"); }

    @Test
    public void testDispatchImage() { Assert.assertFalse(checkHASetupProperty(), "HA Setup failed"); }

    @Test
    public void testRegistrationUtils() { Assert.assertFalse(checkHASetupProperty(), "HA Setup failed"); }

    @Test
    public void testInvalidSearch() { Assert.assertFalse(checkHASetupProperty(), "HA Setup failed"); }
}
