// (C) Copyright 2003-2015 Hewlett-Packard Development Company, L.P.

package com.hp.octane.plugins.jenkins.tests.xml;

import com.hp.octane.plugins.jenkins.tests.TestResult;
import com.hp.octane.plugins.jenkins.tests.TestResultContainer;
import com.hp.octane.plugins.jenkins.tests.TestResultIterable;
import com.hp.octane.plugins.jenkins.tests.TestResultIterator;
import com.hp.octane.plugins.jenkins.tests.TestResultStatus;
import com.hp.octane.plugins.jenkins.tests.TestUtils;
import com.hp.octane.plugins.jenkins.tests.detection.ResultFields;
import hudson.FilePath;
import hudson.matrix.Axis;
import hudson.matrix.AxisList;
import hudson.matrix.MatrixBuild;
import hudson.matrix.MatrixProject;
import hudson.model.AbstractBuild;
import hudson.model.FreeStyleBuild;
import hudson.model.FreeStyleProject;
import org.junit.Assert;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule;

import javax.xml.stream.XMLStreamException;
import java.io.File;
import java.io.IOException;
import java.util.Collections;

public class TestResultXmlWriterTest {

    @Rule
    final public JenkinsRule jenkins = new JenkinsRule();

    private TestResultContainer container;

    @Before
    public void initialize() throws IOException {
        container = new TestResultContainer(Collections.singleton(new TestResult("module", "package", "class", "testName", TestResultStatus.PASSED, 1l, 2l, null)).iterator(), new ResultFields());
    }

    @Test
    public void testNonEmptySubType() throws Exception {
        MatrixProject matrixProject = jenkins.createMatrixProject("matrix-project");
        matrixProject.setAxes(new AxisList(new Axis("OS", "Linux")));
        MatrixBuild build = (MatrixBuild) TestUtils.runAndCheckBuild(matrixProject);
        Assert.assertEquals(1, build.getExactRuns().size());
        assertBuildType(build.getExactRuns().get(0), "matrix-project", "OS=Linux");
    }

    @Test
    public void testEmptySubType() throws Exception {
        FreeStyleProject project = jenkins.createFreeStyleProject("freestyle-project");
        FreeStyleBuild build = (FreeStyleBuild) TestUtils.runAndCheckBuild(project);
        assertBuildType(build, "freestyle-project", null);
    }

    private void assertBuildType(AbstractBuild build, String buildType, String subType) throws IOException, XMLStreamException, InterruptedException {
        FilePath testXml = new FilePath(build.getWorkspace(), "test.xml");
        TestResultXmlWriter xmlWriter = new TestResultXmlWriter(testXml, build);
        xmlWriter.add(container, null);
        xmlWriter.close();

        TestResultIterator iterator = new TestResultIterable(new File(testXml.getRemote())).iterator();
        Assert.assertEquals(buildType, iterator.getBuildType());
        Assert.assertEquals(subType, iterator.getSubType());
    }
}
