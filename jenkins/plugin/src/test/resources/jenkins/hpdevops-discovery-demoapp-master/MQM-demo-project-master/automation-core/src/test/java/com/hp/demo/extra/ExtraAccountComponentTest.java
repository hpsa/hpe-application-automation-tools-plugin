package com.hp.demo.extra;

import com.hp.demo.support.AbstractTest;
import com.hp.demo.support.SystemTests;
import org.junit.Assert;
import org.junit.Test;
import org.junit.experimental.categories.Category;

/**
 * This is an example of a jUnit test class. Feel free to add new test methods, test classes, ...
 */
@Category(SystemTests.class)
public class ExtraAccountComponentTest extends AbstractTest {
    @Test
    public void testTestFindAccountAction() {
        //this is an example of slow test - uncomment if you want a slow test
        //slowTest();
    }

    @Test
    public void testAddAccountAction() {
        //example of occasionally failing test - uncomment if you want such test
        //Assert.assertFalse("Add account has failed.", successfulBuild());
    }

    @Test
    public void testExtraListener() {
        //standard test doing something
        Assert.assertFalse("HA Setup failed", checkHASetupProperty());
    }

    @Test
    public void testRemoveAccount() {
        //another standard test doing something
        Assert.assertFalse("HA Setup failed", checkHASetupProperty());
    }

    @Test
    public void testValidateAccount() {
        //another standard test doing something
        Assert.assertFalse("HA Setup failed", checkHASetupProperty());
    }
}
