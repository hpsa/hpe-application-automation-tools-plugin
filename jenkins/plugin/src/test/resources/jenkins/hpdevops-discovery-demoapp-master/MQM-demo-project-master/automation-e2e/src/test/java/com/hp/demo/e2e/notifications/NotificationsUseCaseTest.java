package com.hp.demo.e2e.notifications;

import com.hp.demo.support.AbstractTest;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Functional
     com.hp.demo.e2e.notifications - NotificationsUseCaseTest - testDelegateHandler
     com.hp.demo.e2e.notifications - NotificationsUseCaseTest - testDelegateComponent
     com.hp.demo.e2e.notifications - NotificationsUseCaseTest - testMergeValidation
     com.hp.demo.e2e.notifications - NotificationsUseCaseTest - testMergePlatform
     com.hp.demo.e2e.notifications - NotificationsUseCaseTest - testCreateAccount
     com.hp.demo.e2e.notifications - NotificationsUseCaseTest - testMergePerformance
     com.hp.demo.e2e.notifications - NotificationsUseCaseTest - testNotFoundCallback
     com.hp.demo.e2e.notifications - NotificationsUseCaseTest - testRegistrationListener
     com.hp.demo.e2e.notifications - NotificationsUseCaseTest - testFoundPerformance
 */
@Test(groups = { "com.hp.demo.support.FunctionalTests" })
public class NotificationsUseCaseTest extends AbstractTest {
    @Test
    public void testDelegateHandler() { Assert.assertFalse(checkHASetupProperty(), "HA Setup failed"); }

    @Test
    public void testDelegateComponent() { Assert.assertFalse(checkHASetupProperty(), "HA Setup failed"); }

    @Test
    public void testMergeValidation() { Assert.assertFalse(checkHASetupProperty(), "HA Setup failed"); }

    @Test
    public void testMergePlatform() { Assert.assertFalse(checkHASetupProperty(), "HA Setup failed"); }

    @Test
    public void testCreateAccount() { Assert.assertFalse(checkHASetupProperty(), "HA Setup failed"); }

    @Test
    public void testCreateTasks() { Assert.assertFalse(checkHASetupProperty(), "HA Setup failed"); }

    @Test
    public void testResetPlatform() { Assert.assertFalse(checkHASetupProperty(), "HA Setup failed"); }

    @Test
    public void testExportNotifications() { Assert.assertFalse(checkHASetupProperty(), "HA Setup failed"); }

}
