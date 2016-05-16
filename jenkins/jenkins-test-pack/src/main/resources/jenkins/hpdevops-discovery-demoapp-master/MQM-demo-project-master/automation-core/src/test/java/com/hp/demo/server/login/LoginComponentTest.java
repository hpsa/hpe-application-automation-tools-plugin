package com.hp.demo.server.login;

import com.hp.demo.support.AbstractTest;
import com.hp.demo.support.SystemTests;
import org.junit.Assert;
import org.junit.Test;
import org.junit.experimental.categories.Category;

/**
 *  System
     com.hp.demo.server.login - LoginComponentTest - testDispatchCamera
     com.hp.demo.server.login - LoginComponentTest - testNullModule
     com.hp.demo.server.login - LoginComponentTest - testResetNegative
     com.hp.demo.server.login - LoginComponentTest - testFoundContact
     com.hp.demo.server.login - LoginComponentTest - testCreateLifecycle
     com.hp.demo.server.login - LoginComponentTest - testNullPerformance
     com.hp.demo.server.login - LoginComponentTest - testFoundListener
     com.hp.demo.server.login - LoginComponentTest - testRegistrationCallback
     com.hp.demo.server.login - LoginComponentTest - testFoundPlatform
 */

@Category(SystemTests.class)
public class LoginComponentTest extends AbstractTest {
    @Test
    //slow test
    public void testDispatchCamera() {
        slowTest();
    }

    @Test
    public void testNullModule() {
        Assert.assertFalse("HA Setup failed", checkHASetupProperty());
    }

    @Test
    public void testResetNegative() {
        Assert.assertFalse("HA Setup failed", checkHASetupProperty());
    }

    @Test
    public void testFoundContact() {
        Assert.assertFalse("HA Setup failed", checkHASetupProperty());
    }

    @Test
    public void testNullPerformance() {
        Assert.assertFalse("HA Setup failed", checkHASetupProperty());
    }

    @Test
    public void testFoundListener() {
        Assert.assertTrue("Listener was not found.", successfulBuild());
        Assert.assertFalse("HA Setup failed",checkHASetupProperty());
    }

    @Test
    public void testRegistrationCallback() {
        Assert.assertFalse("HA Setup failed", checkHASetupProperty());
    }

    @Test
    public void testFoundPlatform() {
        Assert.assertFalse("HA Setup failed", checkHASetupProperty());
    }

}
