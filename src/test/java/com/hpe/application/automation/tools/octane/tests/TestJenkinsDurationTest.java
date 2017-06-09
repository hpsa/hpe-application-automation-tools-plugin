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

package com.hpe.application.automation.tools.octane.tests;

import hudson.model.FreeStyleBuild;
import hudson.model.FreeStyleProject;
import org.junit.Assert;
import org.junit.ClassRule;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule;

import static org.junit.Assert.assertEquals;

/**
 * Created by berkovir on 06/02/2017.
 */
@SuppressWarnings({"squid:S2698","squid:S2699","squid:S3658","squid:S2259","squid:S1872","squid:S2925"})
public class TestJenkinsDurationTest {

    @ClassRule
    public static final JenkinsRule rule = new JenkinsRule();


    @Test
    public void testDuration() throws Exception {

        FreeStyleProject p = rule.createFreeStyleProject("test-duration");

        assertEquals(0, p.getBuilds().toArray().length);
        long start = System.currentTimeMillis();
        p.scheduleBuild2(0);

        while (p.getLastBuild() == null || p.getLastBuild().isBuilding()) {
            Thread.sleep(100);
        }

        long end = System.currentTimeMillis();
        FreeStyleBuild run1 = p.getBuilds().getLastBuild();

        long buildDurationWithoutPostProcessTime = run1.getDuration();
        long buildDurationTotal = (end - start);
        long pluginPostProcessWorkTime = buildDurationTotal - buildDurationWithoutPostProcessTime;


        long buildDurationTotalExpected = 1750;
        long pluginPostProcessWorkTimeExpected = 140;

        System.out.println(String.format("buildDurationTotal=%d, expected=%d", buildDurationTotal, buildDurationTotalExpected));
        System.out.println(String.format("pluginPostProcessWorkTime=%d, expected=%d", pluginPostProcessWorkTime, pluginPostProcessWorkTimeExpected));
        Assert.assertTrue(buildDurationTotal < buildDurationTotalExpected);
        Assert.assertTrue(pluginPostProcessWorkTime < pluginPostProcessWorkTimeExpected);

        int t;

    }
}
