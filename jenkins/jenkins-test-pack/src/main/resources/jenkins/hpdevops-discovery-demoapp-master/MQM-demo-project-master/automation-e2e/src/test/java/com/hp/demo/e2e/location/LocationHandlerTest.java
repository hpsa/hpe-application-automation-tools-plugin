package com.hp.demo.e2e.location;

import com.hp.demo.support.AbstractTest;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Functional
     com.hp.demo.e2e.location - LocationHandlerTest - testCreateAccount
     com.hp.demo.e2e.location - LocationHandlerTest - testExportComponent
     com.hp.demo.e2e.location - LocationHandlerTest - testMergeHandler
     com.hp.demo.e2e.location - LocationHandlerTest - testMergeValidation
 */
@Test(groups = { "com.hp.demo.support.FunctionalTests" })
public class LocationHandlerTest extends AbstractTest {
    @Test
    public void testCreateAccount() { Assert.assertFalse(checkHASetupProperty(), "HA Setup failed"); }

    @Test
    public void testExportComponent() { Assert.assertFalse(checkHASetupProperty(), "HA Setup failed"); }

    @Test
    public void testMergeHandler() { Assert.assertFalse(checkHASetupProperty(), "HA Setup failed"); }

    @Test
    public void testMergeValidation() { Assert.assertFalse(checkHASetupProperty(), "HA Setup failed"); }
}
