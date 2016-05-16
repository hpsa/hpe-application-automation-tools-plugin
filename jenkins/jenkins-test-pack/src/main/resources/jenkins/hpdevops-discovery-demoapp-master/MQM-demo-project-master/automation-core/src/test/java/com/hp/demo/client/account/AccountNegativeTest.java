package com.hp.demo.client.account;

import com.hp.demo.support.AbstractTest;
import com.hp.demo.support.UnitTests;
import org.junit.Assert;
import org.junit.Test;
import org.junit.experimental.categories.Category;

/**
 * Unit
     com.hp.demo.client.account - AccountNegativeTest - testDispatchPerformance
     com.hp.demo.client.account - AccountNegativeTest - testInvalidHandler
     com.hp.demo.client.account - AccountNegativeTest - testMergeTasks
     com.hp.demo.client.account - AccountNegativeTest - testFoundAccount
     com.hp.demo.client.account - AccountNegativeTest - testSimplePerformance
     com.hp.demo.client.account - AccountNegativeTest - testFoundFactory
     com.hp.demo.client.account - AccountNegativeTest - testInvalidCallback
 */

@Category(UnitTests.class)
public class AccountNegativeTest extends AbstractTest {

    @Test
    public void testDispatchPerformance() { Assert.assertFalse("HA Setup failed", checkHASetupProperty()); }

    @Test
    public void testInvalidHandler() { Assert.assertFalse("HA Setup failed", checkHASetupProperty()); }

    @Test
    public void testMergeTasks() { Assert.assertFalse("HA Setup failed", checkHASetupProperty()); }

    @Test
    public void testFoundAccount() { Assert.assertFalse("HA Setup failed", checkHASetupProperty()); }

    @Test
    public void testSimplePerformance() { Assert.assertFalse("HA Setup failed", checkHASetupProperty()); }

    @Test
    public void testFoundFactory() { Assert.assertFalse("HA Setup failed", checkHASetupProperty()); }

    @Test
    public void testInvalidCallback() { Assert.assertFalse("HA Setup failed", checkHASetupProperty()); }
}
