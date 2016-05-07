package com.hp.demo.extra;

import com.hp.demo.support.AbstractTest;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * This is an example of a testNG test class. Feel free to add new test methods, test classes, ...
 */
@Test(groups = "com.hp.demo.support.RESTTests")
public class ExtraLoginComponentTest extends AbstractTest {
    @Test
    public void testTestLoginAction() {
        //this is an example of slow test - uncomment if you want a slow test
        //slowTest();
    }
    @Test
    public void testLogOutAction() {
        //example of occasionally failing test - uncomment if you want such test
        //Assert.assertFalse(successfulBuild(), "Log out action has failed.");
    }

    @Test
    public void testRegisterExtraListener() {
        //standard test doing something
        Assert.assertFalse(checkHASetupProperty(), "HA Setup failed");
    }

    @Test
    public void testLDAPNotAvailable() {
        //another standard test doing something
        Assert.assertFalse(checkHASetupProperty(), "HA Setup failed");
    }

    @Test
    public void testLDAP() {
        //another standard test doing something
        Assert.assertFalse(checkHASetupProperty(), "HA Setup failed");
    }
}
