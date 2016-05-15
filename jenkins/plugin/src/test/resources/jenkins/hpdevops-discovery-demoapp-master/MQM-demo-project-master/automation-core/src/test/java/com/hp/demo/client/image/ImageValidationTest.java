package com.hp.demo.client.image;

import com.hp.demo.support.AbstractTest;
import com.hp.demo.support.UnitTests;
import org.junit.Assert;
import org.junit.Test;
import org.junit.experimental.categories.Category;

/**
 * Unit
     com.hp.demo.client.image - ImageValidationTest - testDispatchValidation
     com.hp.demo.client.image - ImageValidationTest - testResetPlatform
     com.hp.demo.client.image - ImageValidationTest - testFoundModule
     com.hp.demo.client.image - ImageValidationTest - testInvalidContact
     com.hp.demo.client.image - ImageValidationTest - testRegistrationFactory
     com.hp.demo.client.image - ImageValidationTest - testExportNotifications
     com.hp.demo.client.image - ImageValidationTest - testNotFoundLogin
 */

@Category(UnitTests.class)
public class ImageValidationTest extends AbstractTest {

    @Test
    public void testDispatchValidation() { Assert.assertFalse("HA Setup failed", checkHASetupProperty()); }

    @Test
    public void testResetPlatform() { Assert.assertFalse("HA Setup failed", checkHASetupProperty()); }

    @Test
    public void testFoundModule() { Assert.assertFalse("HA Setup failed", checkHASetupProperty()); }

    @Test
    public void testInvalidContact() { Assert.assertFalse("HA Setup failed", checkHASetupProperty()); }

    @Test
    public void testRegistrationFactory() { Assert.assertFalse("HA Setup failed", checkHASetupProperty()); }

    @Test
    public void testExportNotifications() { Assert.assertFalse("HA Setup failed", checkHASetupProperty()); }

    @Test
    public void testNotFoundLogin() { Assert.assertFalse("HA Setup failed", checkHASetupProperty()); }
}
