package com.hp.demo.server.utils;

import com.hp.demo.support.AbstractTest;
import com.hp.demo.support.UnitTests;
import org.junit.Assert;
import org.junit.Test;
import org.junit.experimental.categories.Category;

/**
 * Unit
     com.hp.demo.server.utils - UtilsListenerTest - testResetFactory
     com.hp.demo.server.utils - UtilsListenerTest - testMergeLogin
     com.hp.demo.server.utils - UtilsListenerTest - testCreateLifecycle
     com.hp.demo.server.utils - UtilsListenerTest - testNullPerformance
     com.hp.demo.server.utils - UtilsListenerTest - testNullEvent
     com.hp.demo.server.utils - UtilsListenerTest - testSimpleModule
     com.hp.demo.server.utils - UtilsListenerTest - testDelegateProviders
     com.hp.demo.server.utils - UtilsListenerTest - testNotFoundNotifications
 */

@Category(UnitTests.class)
public class UtilsListenerTest extends AbstractTest {

    @Test
    public void testResetFactory() { Assert.assertFalse("HA Setup failed", checkHASetupProperty()); }

    @Test
    public void testMergeLogin() { Assert.assertFalse("HA Setup failed", checkHASetupProperty()); }

    @Test
    public void testCreateLifecycle() { Assert.assertFalse("HA Setup failed", checkHASetupProperty()); }

    @Test
    public void testNullPerformance() { Assert.assertFalse("HA Setup failed", checkHASetupProperty()); }

    @Test
    public void testNullEvent() { Assert.assertFalse("HA Setup failed", checkHASetupProperty()); }

    @Test
    public void testSimpleModule() { Assert.assertFalse("HA Setup failed", checkHASetupProperty()); }

    @Test
    public void testDelegateProviders() { Assert.assertFalse("HA Setup failed", checkHASetupProperty()); }

    @Test
    public void testNotFoundNotifications() { Assert.assertFalse("HA Setup failed", checkHASetupProperty()); }
}
