package com.hpe.application.automation.tools.octane.identity;

import com.hpe.application.automation.tools.octane.configuration.ConfigurationService;
import org.junit.Assert;
import org.junit.ClassRule;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule;

public class ServerIdentityTest {

    @ClassRule
    public static final JenkinsRule rule = new JenkinsRule();

    @Test
    public void testIdentity() throws Exception {
        String identity = ConfigurationService.getModel().getIdentity();
        Assert.assertNotNull(identity);
        Assert.assertFalse(identity.isEmpty());

        String identity2 = ConfigurationService.getModel().getIdentity();
        Assert.assertEquals(identity2, identity);
    }
}
