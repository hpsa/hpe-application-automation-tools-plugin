package com.hp.demo.client.platform;

import com.hp.demo.support.AbstractTest;
import com.hp.demo.support.UnitTests;
import org.junit.Assert;
import org.junit.Test;
import org.junit.experimental.categories.Category;

/**
 * Unit
     com.hp.demo.client.platform - PlatformNegativeTest - testRegistrationFactory
     com.hp.demo.client.platform - PlatformNegativeTest - testNullNotifications
     com.hp.demo.client.platform - PlatformNegativeTest - testExportNegative
     com.hp.demo.client.platform - PlatformNegativeTest - testMergeLifecycle
     com.hp.demo.client.platform - PlatformNegativeTest - testDispatchModule
     com.hp.demo.client.platform - PlatformNegativeTest - testFoundImage
     com.hp.demo.client.platform - PlatformNegativeTest - testExportSearch
     com.hp.demo.client.platform - PlatformNegativeTest - testMergeFactory
     com.hp.demo.client.platform - PlatformNegativeTest - testResetLifecycle
 */

@Category(UnitTests.class)
public class PlatformNegativeTest extends AbstractTest {

    @Test
    public void testRegistrationFactory() { Assert.assertFalse("HA Setup failed", checkHASetupProperty()); }

    @Test
    public void testNullNotifications() { Assert.assertFalse("HA Setup failed", checkHASetupProperty()); }

    @Test
    public void testExportNegative() { Assert.assertFalse("HA Setup failed", checkHASetupProperty()); }

    @Test
    public void testMergeLifecycle() { Assert.assertFalse("HA Setup failed", checkHASetupProperty()); }

    @Test
    public void testDispatchModule() { Assert.assertFalse("HA Setup failed", checkHASetupProperty()); }

    @Test
    public void testFoundImage() { Assert.assertFalse("HA Setup failed", checkHASetupProperty()); }

    @Test
    public void testExportSearch() { Assert.assertFalse("HA Setup failed", checkHASetupProperty()); }

    @Test
    public void testMergeFactory() { Assert.assertFalse("HA Setup failed", checkHASetupProperty()); }

    @Test
    public void testResetLifecycle() { Assert.assertFalse("HA Setup failed", checkHASetupProperty()); }
}