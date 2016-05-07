package com.hp.demo.client.login;

import com.hp.demo.support.AbstractTest;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * REST
     com.hp.demo.client.login - LoginEventTest - testResetValidation
     com.hp.demo.client.login - LoginEventTest - testCreateImage
     com.hp.demo.client.login - LoginEventTest - testResetNotifications
     com.hp.demo.client.login - LoginEventTest - testSimpleListener
     com.hp.demo.client.login - LoginEventTest - testCreateAccount
 */
@Test(groups = "com.hp.demo.support.RESTTests")
public class LoginEventTest extends AbstractTest {
    @Test
    public void testResetValidation() {
        Assert.assertFalse(checkHASetupProperty(), "HA Setup failed"); }

    @Test
    public void testCreateImage() { Assert.assertFalse(checkHASetupProperty(), "HA Setup failed");}

    @Test
    public void testResetNotifications() {Assert.assertFalse(checkHASetupProperty(), "HA Setup failed"); }

    @Test
    public void testSimpleListener() {Assert.assertFalse(checkHASetupProperty(), "HA Setup failed"); }

    @Test
    public void testCreateAccount() {Assert.assertFalse(checkHASetupProperty(), "HA Setup failed"); }
}
