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
public class AddingBCustomTest extends AbstractTest {
    @Test
    public void testMyBExtraCode() {
        System.out.println("Adding a new system B test - brand new");
        // this test does not support High-Availability setup and will fail
        Assert.assertFalse("HA Setup failed", checkHASetupProperty());
    }

    @Test
    public void testMyBExtraCodeLazyInit() {
        System.out.println("Adding a new system B test - lazy init");
        // this test does not support High-Availability setup and will fail
        Assert.assertFalse("HA Setup failed", checkHASetupProperty());
    }
}
