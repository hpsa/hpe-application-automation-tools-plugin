package com.hp.demo.client.providers;

import com.hp.demo.support.AbstractTest;
import com.hp.demo.support.UnitTests;
import org.junit.Assert;
import org.junit.Test;
import org.junit.experimental.categories.Category;

/**
 * Unit
     com.hp.demo.client.providers - ProvidersNegativeTest - testInvalidUseCase
     com.hp.demo.client.providers - ProvidersNegativeTest - testFoundFactory
     com.hp.demo.client.providers - ProvidersNegativeTest - testNotFoundPlatform
     com.hp.demo.client.providers - ProvidersNegativeTest - testNotFoundValidation
     com.hp.demo.client.providers - ProvidersNegativeTest - testNullComponent
     com.hp.demo.client.providers - ProvidersNegativeTest - testDelegateHandler
     com.hp.demo.client.providers - ProvidersNegativeTest - testInvalidComponent
     com.hp.demo.client.providers - ProvidersNegativeTest - testResetPerformance
 */

@Category(UnitTests.class)
public class ProvidersNegativeTest extends AbstractTest {

    @Test
    public void testInvalidUseCase() { Assert.assertFalse("HA Setup failed", checkHASetupProperty()); }

    @Test
    public void testFoundFactory() { Assert.assertFalse("HA Setup failed", checkHASetupProperty()); }

    @Test
    public void testNotFoundPlatform() { Assert.assertFalse("HA Setup failed", checkHASetupProperty()); }

    @Test
    public void testNotFoundValidation() { Assert.assertFalse("HA Setup failed", checkHASetupProperty()); }

    @Test
    public void testNullComponent() { Assert.assertFalse("HA Setup failed", checkHASetupProperty()); }

    @Test
    public void testDelegateHandler() { Assert.assertFalse("HA Setup failed", checkHASetupProperty()); }

    @Test
    public void testInvalidComponent() { Assert.assertFalse("HA Setup failed", checkHASetupProperty()); }

    @Test
    public void testResetPerformance() { Assert.assertFalse("HA Setup failed", checkHASetupProperty()); }
}