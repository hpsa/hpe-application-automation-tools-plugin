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

package com.hpe.application.automation.tools.octane.configuration;

import com.hpe.application.automation.tools.octane.tests.ExtensionUtil;
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

        MatrixProject matrixProject = jenkins.createProject(MatrixProject.class, "ConfigurationActionFactoryTest.testMatrixJob");
        //jenkins.createMatrixProject("ConfigurationActionFactoryTest.testMatrixJob");
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
