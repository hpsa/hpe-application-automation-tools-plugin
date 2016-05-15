package com.hp.demo.server.camera;

import com.hp.demo.support.AbstractTest;
import com.hp.demo.support.UnitTests;
import org.junit.Assert;
import org.junit.Test;
import org.junit.experimental.categories.Category;

/**
 * Unit
     com.hp.demo.server.camera - CameraListenerModuleTest - testMergeContact
     com.hp.demo.server.camera - CameraListenerModuleTest - testMergeCamera
     com.hp.demo.server.camera - CameraListenerModuleTest - testResetCamera
     com.hp.demo.server.camera - CameraListenerModuleTest - testSimpleHandler
     com.hp.demo.server.camera - CameraListenerModuleTest - testExportLifecycle
     com.hp.demo.server.camera - CameraListenerModuleTest - testSimpleTasks
     com.hp.demo.server.camera - CameraListenerModuleTest - testFoundEvent
     com.hp.demo.server.camera - CameraListenerModuleTest - testResetListener
 */

@Category(UnitTests.class)
public class CameraListenerModuleTest extends AbstractTest {

    @Test
    public void testMergeContact() { Assert.assertFalse("HA Setup failed", checkHASetupProperty()); }

    @Test
    public void testMergeCamera() { Assert.assertFalse("HA Setup failed", checkHASetupProperty()); }

    @Test
    public void testResetCamera() { Assert.assertFalse("HA Setup failed", checkHASetupProperty()); }

    @Test
    public void testSimpleHandler() { Assert.assertFalse("HA Setup failed", checkHASetupProperty()); }

    @Test
    public void testExportLifecycle() { Assert.assertFalse("HA Setup failed", checkHASetupProperty()); }

    @Test
    public void testSimpleTasks() { Assert.assertFalse("HA Setup failed", checkHASetupProperty()); }

    @Test
    public void testFoundEvent() { Assert.assertFalse("HA Setup failed", checkHASetupProperty()); }

    @Test
    public void testResetListener() { Assert.assertFalse("HA Setup failed", checkHASetupProperty()); }
}
