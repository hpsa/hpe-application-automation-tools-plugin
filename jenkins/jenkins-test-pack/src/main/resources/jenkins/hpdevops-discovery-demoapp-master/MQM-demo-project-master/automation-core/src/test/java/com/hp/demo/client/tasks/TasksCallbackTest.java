package com.hp.demo.client.tasks;

import com.hp.demo.support.AbstractTest;
import com.hp.demo.support.UnitTests;
import org.junit.Assert;
import org.junit.Test;
import org.junit.experimental.categories.Category;

/**
 * Unit
     com.hp.demo.client.tasks - TasksCallbackTest - testNotFoundRest
     com.hp.demo.client.tasks - TasksCallbackTest - testResetLifecycle
     com.hp.demo.client.tasks - TasksCallbackTest - testResetPlatform
     com.hp.demo.client.tasks - TasksCallbackTest - testExportLifecycle
     com.hp.demo.client.tasks - TasksCallbackTest - testResetLocation
     com.hp.demo.client.tasks - TasksCallbackTest - testInvalidAccount
     com.hp.demo.client.tasks - TasksCallbackTest - testInvalidNegative
 */

@Category(UnitTests.class)
public class TasksCallbackTest extends AbstractTest {

    @Test
    public void testNotFoundRest() { Assert.assertFalse("HA Setup failed", checkHASetupProperty()); }

    @Test
    public void testResetLifecycle() { Assert.assertFalse("HA Setup failed", checkHASetupProperty()); }

    @Test
    public void testResetPlatform() { Assert.assertFalse("HA Setup failed", checkHASetupProperty()); }

    @Test
    public void testExportLifecycle() { Assert.assertFalse("HA Setup failed", checkHASetupProperty()); }

    @Test
    public void testResetLocation() { Assert.assertFalse("HA Setup failed", checkHASetupProperty()); }

    @Test
    public void testInvalidAccount() { Assert.assertFalse("HA Setup failed", checkHASetupProperty()); }

    @Test
    public void testInvalidNegative() { Assert.assertFalse("HA Setup failed", checkHASetupProperty()); }
}
