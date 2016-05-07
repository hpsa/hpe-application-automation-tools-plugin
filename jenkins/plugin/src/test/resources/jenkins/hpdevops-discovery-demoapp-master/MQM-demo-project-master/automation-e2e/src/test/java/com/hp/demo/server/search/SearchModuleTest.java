package com.hp.demo.server.search;

import com.hp.demo.support.AbstractTest;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * REST
     com.hp.demo.server.search - SearchModuleTest - testDispatchLifecycle
     com.hp.demo.server.search - SearchModuleTest - testNotFoundSearch
     com.hp.demo.server.search - SearchModuleTest - testNotFoundLifecycle
     com.hp.demo.server.search - SearchModuleTest - testCreateListener
     com.hp.demo.server.search - SearchModuleTest - testDelegateValidation
     com.hp.demo.server.search - SearchModuleTest - testInvalidComponent
 */
@Test(groups = "com.hp.demo.support.RESTTests")
public class SearchModuleTest extends AbstractTest {
    @Test
    public void testDispatchLifecycle() { Assert.assertFalse(checkHASetupProperty(), "HA Setup failed");}

    @Test
    public void testNotFoundSearch() {Assert.assertFalse(checkHASetupProperty(), "HA Setup failed"); }

    @Test
    public void testNotFoundLifecycle() {Assert.assertFalse(checkHASetupProperty(), "HA Setup failed"); }

    @Test
    public void testCreateListener() { Assert.assertFalse(checkHASetupProperty(), "HA Setup failed");}

    @Test
    public void testDelegateValidation() { Assert.assertFalse(checkHASetupProperty(), "HA Setup failed");}

    @Test
    public void testInvalidComponent() {Assert.assertFalse(checkHASetupProperty(), "HA Setup failed"); }
}
