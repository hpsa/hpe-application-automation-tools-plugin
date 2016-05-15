package com.hp.demo.server.tasks;

import com.hp.demo.support.AbstractTest;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * REST
     com.hp.demo.server.tasks - TasksModuleTest - testDispatchCallback
     com.hp.demo.server.tasks - TasksModuleTest - testFoundCamera
     com.hp.demo.server.tasks - TasksModuleTest - testRegistrationSearch
     com.hp.demo.server.tasks - TasksModuleTest - testFoundValidation
 */
@Test(groups = "com.hp.demo.support.RESTTests")
public class TasksModuleTest extends AbstractTest {
    @Test
    public void testDispatchCallback() {
        Assert.assertFalse(checkHASetupProperty(), "HA Setup failed"); }

    @Test
    public void testFoundCamera() { Assert.assertFalse(checkHASetupProperty(), "HA Setup failed");}

    @Test
    public void testRegistrationSearch() { Assert.assertFalse(checkHASetupProperty(), "HA Setup failed");}

    @Test
    public void testFoundValidation() {Assert.assertFalse(checkHASetupProperty(), "HA Setup failed"); }
}
