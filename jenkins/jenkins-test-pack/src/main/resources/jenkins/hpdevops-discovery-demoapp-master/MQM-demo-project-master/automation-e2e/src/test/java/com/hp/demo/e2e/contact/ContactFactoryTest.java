package com.hp.demo.e2e.contact;

import com.hp.demo.support.AbstractTest;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Functional
     com.hp.demo.e2e.contact - ContactFactoryTest - testNotFoundSearch
     com.hp.demo.e2e.contact - ContactFactoryTest - testExportValidation
     com.hp.demo.e2e.contact - ContactFactoryTest - testInvalidUseCase
     com.hp.demo.e2e.contact - ContactFactoryTest - testNullPerformance
     com.hp.demo.e2e.contact - ContactFactoryTest - testDispatchTasks
     com.hp.demo.e2e.contact - ContactFactoryTest - testNotFoundHandler
 */
@Test(groups = { "com.hp.demo.support.FunctionalTests" })
public class ContactFactoryTest extends AbstractTest {
    @Test
    public void testNotFoundSearch() { Assert.assertFalse(checkHASetupProperty(), "HA Setup failed"); }

    @Test
    public void testExportValidation() { Assert.assertFalse(checkHASetupProperty(), "HA Setup failed"); }

    @Test
    public void testInvalidUseCase() { Assert.assertFalse(checkHASetupProperty(), "HA Setup failed"); }

    @Test
    public void testNullPerformance() { Assert.assertFalse(checkHASetupProperty(), "HA Setup failed"); }

    @Test
    public void testDispatchTasks() { Assert.assertFalse(checkHASetupProperty(), "HA Setup failed"); }

    @Test
    public void testNotFoundHandler() { Assert.assertFalse(checkHASetupProperty(), "HA Setup failed"); }
}
