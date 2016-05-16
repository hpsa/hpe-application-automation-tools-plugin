package com.hp.demo.client.rest;

import com.hp.demo.support.AbstractTest;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * REST
     com.hp.demo.server.rest - RestHandlerTest - testSimpleLifecycle
     com.hp.demo.server.rest - RestHandlerTest - testNotFoundProviders
     com.hp.demo.server.rest - RestHandlerTest - testNullAccount
     com.hp.demo.server.rest - RestHandlerTest - testCreateHandler
     com.hp.demo.server.rest - RestHandlerTest - testNullFactory
     com.hp.demo.server.rest - RestHandlerTest - testNotFoundListener
     com.hp.demo.server.rest - RestHandlerTest - testNotFoundCallback
 */
@Test(groups = "com.hp.demo.support.RESTTests")
public class RestHandlerTest extends AbstractTest {
    @Test
    public void testSimpleLifecycle() {
        Assert.assertFalse(checkHASetupProperty(), "HA Setup failed"); }

    @Test
    public void testNotFoundProviders() {Assert.assertFalse(checkHASetupProperty(), "HA Setup failed"); }

    @Test
    public void testNullAccount() {Assert.assertFalse(checkHASetupProperty(), "HA Setup failed"); }

    @Test
    public void testCreateHandler() {Assert.assertFalse(checkHASetupProperty(), "HA Setup failed"); }

    @Test
    public void testNullFactory() {Assert.assertFalse(checkHASetupProperty(), "HA Setup failed"); }

    @Test
    public void testNotFoundListener() {Assert.assertFalse(checkHASetupProperty(), "HA Setup failed"); }

    @Test
    public void testNotFoundCallback() {Assert.assertFalse(checkHASetupProperty(), "HA Setup failed"); }
}
