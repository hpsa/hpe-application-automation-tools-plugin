// (C) Copyright 2003-2015 Hewlett-Packard Development Company, L.P.

package com.hpe.application.automation.tools.octane.tests;

import hudson.ExtensionList;
import org.junit.Assert;
import org.jvnet.hudson.test.JenkinsRule;

public class ExtensionUtil {

    public static <E> E getInstance(JenkinsRule rule, Class<E> clazz) {
        ExtensionList<E> items = rule.getInstance().getExtensionList(clazz);
        Assert.assertEquals(1, items.size());
        return items.get(0);
    }
}
