package com.hp.demo.client.camera;

import com.hp.demo.support.AbstractTest;
import com.hp.demo.support.UnitTests;
import org.junit.Assert;
import org.junit.Test;
import org.junit.experimental.categories.Category;

/**
 * Unit
     com.hp.demo.client.camera - CameraModuleTest - testSimpleComponent
     com.hp.demo.client.camera - CameraModuleTest - testResetAccount
     com.hp.demo.client.camera - CameraModuleTest - testFoundNegative
     com.hp.demo.client.camera - CameraModuleTest - testMergeCallback
     com.hp.demo.client.camera - CameraModuleTest - testMergeListener
     com.hp.demo.client.camera - CameraModuleTest - testNotFoundValidation
     com.hp.demo.client.camera - CameraModuleTest - testSimpleModule
     com.hp.demo.client.camera - CameraModuleTest - testExportPlatform
     com.hp.demo.client.camera - CameraModuleTest - testFoundModule
 */

@Category(UnitTests.class)
public class CameraModuleTest extends AbstractTest {

    @Test
    public void testSimpleComponent() { Assert.assertFalse("HA Setup failed", checkHASetupProperty()); }

    @Test
    public void testResetAccount() { Assert.assertFalse("HA Setup failed", checkHASetupProperty()); }

    @Test
    public void testFoundNegative() { Assert.assertFalse("HA Setup failed", checkHASetupProperty()); }

    @Test
    public void testMergeCallback() { Assert.assertFalse("HA Setup failed", checkHASetupProperty()); }

    @Test
    public void testNotFoundValidation() { Assert.assertFalse("HA Setup failed", checkHASetupProperty()); }

    @Test
    public void testSimpleModule() { Assert.assertFalse("HA Setup failed", checkHASetupProperty()); }

    @Test
    public void testExportPlatform() { Assert.assertFalse("HA Setup failed", checkHASetupProperty()); }

    @Test
    public void testFoundModule() { Assert.assertFalse("HA Setup failed", checkHASetupProperty()); }
}
