package com.hp.demo.server.contact;

import com.hp.demo.support.AbstractTest;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * REST
     com.hp.demo.server.contact - ContactNegativeTest - testExportRest
     com.hp.demo.server.contact - ContactNegativeTest - testDispatchHandler
     com.hp.demo.server.contact - ContactNegativeTest - testNullProviders
     com.hp.demo.server.contact - ContactNegativeTest - testDelegateValidation
     com.hp.demo.server.contact - ContactNegativeTest - testNotFoundListener
     com.hp.demo.server.contact - ContactNegativeTest - testInvalidPlatform
     com.hp.demo.server.contact - ContactNegativeTest - testNullFactory
     com.hp.demo.server.contact - ContactNegativeTest - testResetEvent
 */
@Test(groups = "com.hp.demo.support.RESTTests")
public class ContactNegativeTest extends AbstractTest {
    @Test
    public void testExportRest() { Assert.assertFalse(checkHASetupProperty(), "HA Setup failed");}

    @Test
    public void testDispatchHandler() {Assert.assertFalse(checkHASetupProperty(), "HA Setup failed"); }

    @Test
    public void testNullProviders() { Assert.assertFalse(checkHASetupProperty(), "HA Setup failed");}

    @Test
    public void testDelegateValidation() { Assert.assertFalse(checkHASetupProperty(), "HA Setup failed");}

    @Test
    public void testNotFoundListener() {Assert.assertFalse(checkHASetupProperty(), "HA Setup failed"); }

    @Test
    public void testInvalidPlatform() {Assert.assertFalse(checkHASetupProperty(), "HA Setup failed"); }

    @Test
    public void testNullFactory() {Assert.assertFalse(checkHASetupProperty(), "HA Setup failed"); }

    @Test
    public void testResetEvent() { Assert.assertFalse(checkHASetupProperty(), "HA Setup failed");}
}
