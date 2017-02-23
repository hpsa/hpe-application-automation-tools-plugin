/*
 *     Copyright 2017 Hewlett-Packard Development Company, L.P.
 *     Licensed under the Apache License, Version 2.0 (the "License");
 *     you may not use this file except in compliance with the License.
 *     You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS,
 *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *     See the License for the specific language governing permissions and
 *     limitations under the License.
 *
 */

package com.hp.octane.plugins.jenkins.identity;

import com.hp.octane.plugins.jenkins.configuration.ConfigurationService;
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
