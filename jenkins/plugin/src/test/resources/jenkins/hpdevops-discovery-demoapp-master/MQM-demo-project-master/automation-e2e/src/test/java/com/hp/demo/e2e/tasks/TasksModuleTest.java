package com.hp.demo.e2e.tasks;

import com.hp.demo.support.AbstractTest;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Functional
     com.hp.demo.e2e.tasks - TasksModuleTest - testDispatchModule
     com.hp.demo.e2e.tasks - TasksModuleTest - testRegistrationAccount
     com.hp.demo.e2e.tasks - TasksModuleTest - testRegistrationPerformance
     com.hp.demo.e2e.tasks - TasksModuleTest - testSimpleCallback
 */
@Test(groups = { "com.hp.demo.support.FunctionalTests" })
public class TasksModuleTest extends AbstractTest {
    @Test
    public void testDispatchModule() { Assert.assertFalse(checkHASetupProperty(), "HA Setup failed"); }

    @Test
    public void testRegistrationAccount() { Assert.assertFalse(checkHASetupProperty(), "HA Setup failed"); }

    @Test
    public void testRegistrationPerformance() { Assert.assertFalse(checkHASetupProperty(), "HA Setup failed"); }

    @Test
    public void testSimpleCallback() { Assert.assertFalse(checkHASetupProperty(), "HA Setup failed"); }
}
