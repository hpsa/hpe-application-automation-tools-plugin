package com.hp.demo.e2e.login;

import com.hp.demo.support.AbstractTest;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Functional
     com.hp.demo.e2e.login - LoginFactoryHandlerTest - testFoundLocation
     com.hp.demo.e2e.login - LoginFactoryHandlerTest - testCreateContact
     com.hp.demo.e2e.login - LoginFactoryHandlerTest - testMergeImage
     com.hp.demo.e2e.login - LoginFactoryHandlerTest - testFoundCamera
     com.hp.demo.e2e.login - LoginFactoryHandlerTest - testInvalidPerformance
     com.hp.demo.e2e.login - LoginFactoryHandlerTest - testCreateTasks
     com.hp.demo.e2e.login - LoginFactoryHandlerTest - testResetPlatform
     com.hp.demo.e2e.login - LoginFactoryHandlerTest - testExportNotifications
 */
@Test(groups = { "com.hp.demo.support.FunctionalTests" })
public class LoginFactoryHandlerTest extends AbstractTest {
    @Test
    public void testFoundLocation() { Assert.assertFalse(checkHASetupProperty(), "HA Setup failed"); }

    @Test
    public void testCreateContact() { Assert.assertFalse(checkHASetupProperty(), "HA Setup failed"); }

    @Test
    public void testMergeImage() { Assert.assertFalse(checkHASetupProperty(), "HA Setup failed"); }

    @Test
    public void testFoundCamera() { Assert.assertFalse(checkHASetupProperty(), "HA Setup failed"); }

    @Test
    public void testInvalidPerformance() { Assert.assertFalse(checkHASetupProperty(), "HA Setup failed"); }

    @Test
    public void testCreateTasks() {
        Assert.assertTrue(successfulBuild(), "Failed creation of tasks.");
        Assert.assertFalse(checkHASetupProperty(), "HA Setup failed");
    }

    @Test
    public void testResetPlatform() { Assert.assertFalse(checkHASetupProperty(), "HA Setup failed"); }

    @Test
    public void testExportNotifications() {
        slowTest();
    }

}
