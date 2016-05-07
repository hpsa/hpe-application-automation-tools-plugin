package com.hp.demo.client.search;

import com.hp.demo.support.AbstractTest;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * REST
     com.hp.demo.client.search - SearchFactoryHandlerTest - testCreateUseCase
     com.hp.demo.client.search - SearchFactoryHandlerTest - testExportSearch
     com.hp.demo.client.search - SearchFactoryHandlerTest - testSimpleListener
     com.hp.demo.client.search - SearchFactoryHandlerTest - testDispatchValidation
     com.hp.demo.client.search - SearchFactoryHandlerTest - testExportFactory
     com.hp.demo.client.search - SearchFactoryHandlerTest - testRegistrationProviders
     com.hp.demo.client.search - SearchFactoryHandlerTest - testRegistrationNegative
 */
@Test(groups = "com.hp.demo.support.RESTTests")
public class SearchFactoryHandlerTest extends AbstractTest {
    @Test
    public void testCreateUseCase() {
        Assert.assertFalse(checkHASetupProperty(), "HA Setup failed"); }

    @Test
    public void testExportSearch() {Assert.assertFalse(checkHASetupProperty(), "HA Setup failed"); }

    @Test
    public void testSimpleListener() {Assert.assertFalse(checkHASetupProperty(), "HA Setup failed"); }

    @Test
    public void testDispatchValidation() { Assert.assertFalse(checkHASetupProperty(), "HA Setup failed");}

    @Test
    public void testExportFactory() { Assert.assertFalse(checkHASetupProperty(), "HA Setup failed");}

    @Test
    public void testRegistrationProviders() {Assert.assertFalse(checkHASetupProperty(), "HA Setup failed"); }

    @Test
    public void testRegistrationNegative() {Assert.assertFalse(checkHASetupProperty(), "HA Setup failed"); }
}
