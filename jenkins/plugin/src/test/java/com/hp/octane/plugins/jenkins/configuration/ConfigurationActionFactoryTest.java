// (C) Copyright 2003-2015 Hewlett-Packard Development Company, L.P.

package com.hp.octane.plugins.jenkins.configuration;

import com.hp.octane.plugins.jenkins.ExtensionUtil;
import hudson.matrix.Axis;
import hudson.matrix.AxisList;
import hudson.matrix.MatrixConfiguration;
import hudson.matrix.MatrixProject;
import hudson.model.FreeStyleProject;
import org.junit.Assert;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule;

import java.io.IOException;

public class ConfigurationActionFactoryTest {

    @ClassRule
    static final public JenkinsRule jenkins = new JenkinsRule();

    private ConfigurationActionFactory configurationActionFactory;

    @Before
    public void initialize() {
        configurationActionFactory = ExtensionUtil.getInstance(jenkins, ConfigurationActionFactory .class);
    }

    @Test
    public void testMatrixJob() throws IOException {
        MatrixProject matrixProject = jenkins.createMatrixProject("ConfigurationActionFactoryTest.testMatrixJob");
        matrixProject.setAxes(new AxisList(new Axis("OS", "Linux", "Windows")));

        Assert.assertEquals(1, configurationActionFactory.createFor(matrixProject).size());
        for (MatrixConfiguration configuration: matrixProject.getItems()) {
            Assert.assertEquals(0, configurationActionFactory.createFor(configuration).size());
        }
    }

    @Test
    public void testFreeStyleJob() throws IOException {
        FreeStyleProject project = jenkins.createFreeStyleProject("ConfigurationActionFactoryTest.testFreeStyleJob");
        Assert.assertEquals(1, configurationActionFactory.createFor(project).size());
    }
}
