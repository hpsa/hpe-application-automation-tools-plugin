package com.hp.octane.plugins.jenkins.tests;

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


        long buildDurationTotalExpected = 1550;
        long pluginPostProcessWorkTimeExpected = 140;

        System.out.println(String.format("buildDurationTotal=%d, expected=%d", buildDurationTotal, buildDurationTotalExpected));
        System.out.println(String.format("pluginPostProcessWorkTime=%d, expected=%d", pluginPostProcessWorkTime, pluginPostProcessWorkTimeExpected));
        Assert.assertTrue(buildDurationTotal < buildDurationTotalExpected);
        Assert.assertTrue(pluginPostProcessWorkTime < pluginPostProcessWorkTimeExpected);

        int t;

    }
}
