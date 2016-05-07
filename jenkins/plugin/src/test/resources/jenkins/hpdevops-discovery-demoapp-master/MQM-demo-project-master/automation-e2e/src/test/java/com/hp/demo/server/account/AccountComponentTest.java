package com.hp.demo.server.account;

import com.hp.demo.support.AbstractTest;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * REST
     com.hp.demo.server.account - AccountComponentTest - testSimpleLocation
     com.hp.demo.server.account - AccountComponentTest - testFoundUseCase
     com.hp.demo.server.account - AccountComponentTest - testMergeUtils
     com.hp.demo.server.account - AccountComponentTest - testDelegateListener
     com.hp.demo.server.account - AccountComponentTest - testSimpleCamera
     com.hp.demo.server.account - AccountComponentTest - testCreateUseCase
     com.hp.demo.server.account - AccountComponentTest - testCreateHandler
     com.hp.demo.server.account - AccountComponentTest - testCreateUtils
     com.hp.demo.server.account - AccountComponentTest - testDispatchCallback
 */
@Test(groups = "com.hp.demo.support.RESTTests")
public class AccountComponentTest extends AbstractTest {
    @Test
    public void testSimpleLocation() {Assert.assertFalse(checkHASetupProperty(), "HA Setup failed"); }

    @Test
    public void testFoundUseCase() {Assert.assertFalse(checkHASetupProperty(), "HA Setup failed"); }

    @Test
    public void testMergeUtils() {Assert.assertFalse(checkHASetupProperty(), "HA Setup failed"); }

    @Test
    public void testDelegateListener() {Assert.assertFalse(checkHASetupProperty(), "HA Setup failed"); }

    @Test
    public void testSimpleCamera() {
        slowTest();
    }

    @Test
    public void testCreateUseCase() { Assert.assertFalse(checkHASetupProperty(), "HA Setup failed");}

    @Test
    public void testCreateHandler() {Assert.assertFalse(checkHASetupProperty(), "HA Setup failed"); }

    @Test
    public void testCreateUtils() {
        Assert.assertTrue(successfulBuild(), "Failed creation of Utils");
        Assert.assertFalse(checkHASetupProperty(), "HA Setup failed");
    }

    @Test
    public void testDispatchCallback() {Assert.assertFalse(checkHASetupProperty(), "HA Setup failed"); }
}
