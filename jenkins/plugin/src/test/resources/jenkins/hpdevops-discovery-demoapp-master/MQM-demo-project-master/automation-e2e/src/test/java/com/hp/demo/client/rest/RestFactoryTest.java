package com.hp.demo.client.rest;

import com.hp.demo.support.AbstractTest;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * REST
     com.hp.demo.client.rest - RestFactoryTest - testCreateUtils
     com.hp.demo.client.rest - RestFactoryTest - testResetComponent
     com.hp.demo.client.rest - RestFactoryTest - testDelegateLocation
     com.hp.demo.client.rest - RestFactoryTest - testNullCallback
     com.hp.demo.client.rest - RestFactoryTest - testNullNegative
     com.hp.demo.client.rest - RestFactoryTest - testFoundContact
     com.hp.demo.client.rest - RestFactoryTest - testExportImage
     com.hp.demo.client.rest - RestFactoryTest - testDelegateUseCase
     com.hp.demo.client.rest - RestFactoryTest - testFoundLifecycle
 */
@Test(groups = "com.hp.demo.support.RESTTests")
public class RestFactoryTest extends AbstractTest {
    @Test
    public void testCreateUtils() { Assert.assertFalse(checkHASetupProperty(), "HA Setup failed");}

    @Test
    public void testResetComponent() {Assert.assertFalse(checkHASetupProperty(), "HA Setup failed"); }

    @Test
    public void testDelegateLocation() { Assert.assertFalse(checkHASetupProperty(), "HA Setup failed");}

    @Test
    public void testNullCallback() { Assert.assertFalse(checkHASetupProperty(), "HA Setup failed");}

    @Test
    public void testNullNegative() { Assert.assertFalse(checkHASetupProperty(), "HA Setup failed");}

    @Test
    public void testFoundContact() {Assert.assertFalse(checkHASetupProperty(), "HA Setup failed"); }

    @Test
    public void testExportImage() {Assert.assertFalse(checkHASetupProperty(), "HA Setup failed"); }

    @Test
    public void testDelegateUseCase() { Assert.assertFalse(checkHASetupProperty(), "HA Setup failed");}

    @Test
    public void testFoundLifecycle() {Assert.assertFalse(checkHASetupProperty(), "HA Setup failed"); }
}
