package com.hp.demo.e2e.camera;

import com.hp.demo.support.AbstractTest;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Functional
     com.hp.demo.e2e.camera - CameraUseCaseTest - testFoundUtils
     com.hp.demo.e2e.camera - CameraUseCaseTest - testCreateUtils
     com.hp.demo.e2e.camera - CameraUseCaseTest - testNullCallback
     com.hp.demo.e2e.camera - CameraUseCaseTest - testMergeNegative
     com.hp.demo.e2e.camera - CameraUseCaseTest - testNullEvent
     com.hp.demo.e2e.camera - CameraUseCaseTest - testDispatchNegative
 */
@Test(groups = { "com.hp.demo.support.FunctionalTests" })
public class CameraUseCaseTest extends AbstractTest {
    @Test
    public void testFoundUtils() { Assert.assertFalse(checkHASetupProperty(), "HA Setup failed"); }

    @Test
    public void testCreateUtils() { Assert.assertFalse(checkHASetupProperty(), "HA Setup failed"); }

    @Test
    public void testNullCallback() { Assert.assertFalse(checkHASetupProperty(), "HA Setup failed"); }

    @Test
    public void testMergeNegative() { Assert.assertFalse(checkHASetupProperty(), "HA Setup failed"); }

    @Test
    public void testNullEvent() { Assert.assertFalse(checkHASetupProperty(), "HA Setup failed"); }

    @Test
    public void testDispatchNegative() { Assert.assertFalse(checkHASetupProperty(), "HA Setup failed"); }
}
