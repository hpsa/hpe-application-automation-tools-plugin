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
public class AddingAnotherCustomTest extends AbstractTest {
    @Test
    public void testMyExtraCode() {
        System.out.println("Adding a new system test - brand new");
        // this test does not support High-Availability setup and will fail
        Assert.assertFalse("HA Setup failed", checkHASetupProperty());
    }

}
