// (C) Copyright 2003-2015 Hewlett-Packard Development Company, L.P.

package com.hp.octane.plugins.jenkins.identity;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule;

public class ServerIdentityTest {

    @Rule
    final public JenkinsRule rule = new JenkinsRule();

    @Test
    public void testIdentity() throws Exception {
        String identity = ServerIdentity.getIdentity();
        Assert.assertNotNull(identity);
        Assert.assertFalse(identity.isEmpty());

        String identity2 = ServerIdentity.getIdentity();
        Assert.assertEquals(identity2, identity);
    }
}
