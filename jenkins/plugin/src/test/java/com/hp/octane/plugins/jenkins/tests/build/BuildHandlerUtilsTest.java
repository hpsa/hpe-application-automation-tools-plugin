// (C) Copyright 2003-2015 Hewlett-Packard Development Company, L.P.

package com.hp.octane.plugins.jenkins.tests.build;

import com.hp.octane.plugins.jenkins.tests.CopyResourceSCM;
import com.hp.octane.plugins.jenkins.tests.TestUtils;
import hudson.matrix.Axis;
import hudson.matrix.AxisList;
import hudson.matrix.MatrixBuild;
import hudson.matrix.MatrixProject;
import hudson.matrix.MatrixRun;
import hudson.maven.MavenModuleSet;
import hudson.maven.MavenModuleSetBuild;
import hudson.model.FreeStyleBuild;
import hudson.model.FreeStyleProject;
import hudson.tasks.Maven;
import org.junit.Assert;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule;

import java.util.HashMap;

public class BuildHandlerUtilsTest {

    @Rule
    final public JenkinsRule jenkins = new JenkinsRule();

    @Test
    public void testMatrixBuildType() throws Exception {
        MatrixProject matrixProject = jenkins.createMatrixProject("matrix-project");
        matrixProject.setAxes(new AxisList(new Axis("OS", "Linux", "Windows")));
        MatrixBuild build = (MatrixBuild) TestUtils.runAndCheckBuild(matrixProject);

        BuildTypeDescriptor descriptor = BuildHandlerUtils.getBuildType(build);
        Assert.assertEquals("matrix-project", descriptor.getBuildType());
        Assert.assertEquals("", descriptor.getSubType());

        Assert.assertEquals("matrix-project", BuildHandlerUtils.getProjectFullName(build));

        HashMap<String, String> expectedType = new HashMap<String, String>();
        expectedType.put("OS=Linux", "matrix-project/OS=Linux");
        expectedType.put("OS=Windows", "matrix-project/OS=Windows");

        for (MatrixRun run: build.getExactRuns()) {
            descriptor = BuildHandlerUtils.getBuildType(run);
            Assert.assertEquals("matrix-project", descriptor.getBuildType());
            String fullName = expectedType.remove(descriptor.getSubType());
            Assert.assertEquals(fullName, BuildHandlerUtils.getProjectFullName(run));
        }
        Assert.assertTrue(expectedType.isEmpty());
    }

    @Test
    public void testMavenBuildType() throws Exception {
        MavenModuleSet project = jenkins.createMavenProject("maven-project");
        Maven.MavenInstallation mavenInstallation = jenkins.configureDefaultMaven();
        project.setMaven(mavenInstallation.getName());
        project.setGoals("test -Dmaven.test.failure.ignore=true");
        project.setScm(new CopyResourceSCM("/helloWorldRoot"));
        MavenModuleSetBuild build = (MavenModuleSetBuild) TestUtils.runAndCheckBuild(project);

        BuildTypeDescriptor descriptor = BuildHandlerUtils.getBuildType(build);
        Assert.assertEquals("maven-project", descriptor.getBuildType());
        Assert.assertEquals("", descriptor.getSubType());

        Assert.assertEquals("maven-project", BuildHandlerUtils.getProjectFullName(build));
    }

    @Test
    public void testFallbackBuildType() throws Exception {
        FreeStyleProject project = jenkins.createFreeStyleProject("freestyle-project");
        FreeStyleBuild build = (FreeStyleBuild) TestUtils.runAndCheckBuild(project);
        BuildTypeDescriptor descriptor = BuildHandlerUtils.getBuildType(build);
        Assert.assertEquals("freestyle-project", descriptor.getBuildType());
        Assert.assertEquals("", descriptor.getSubType());

        Assert.assertEquals("freestyle-project", BuildHandlerUtils.getProjectFullName(build));
    }
}
