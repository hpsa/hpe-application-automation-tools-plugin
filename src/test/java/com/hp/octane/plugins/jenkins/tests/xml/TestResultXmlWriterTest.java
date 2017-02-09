package com.hp.octane.plugins.jenkins.tests.xml;

import com.hp.octane.plugins.jenkins.tests.junit.JUnitTestResult;
import com.hp.octane.plugins.jenkins.tests.TestResultContainer;
import com.hp.octane.plugins.jenkins.tests.TestResultIterable;
import com.hp.octane.plugins.jenkins.tests.TestResultIterator;
import com.hp.octane.plugins.jenkins.tests.junit.TestResultStatus;
import com.hp.octane.plugins.jenkins.tests.TestUtils;
import com.hp.octane.plugins.jenkins.tests.detection.ResultFields;
import com.hp.octane.plugins.jenkins.tests.testResult.TestResult;
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
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule;

import javax.xml.stream.XMLStreamException;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class TestResultXmlWriterTest {

	@ClassRule
	public static final JenkinsRule jenkins = new JenkinsRule();

	private TestResultContainer container;

	@Before
	public void initialize() throws IOException {
		List<TestResult> testResults = new ArrayList<>();
		testResults.add(new JUnitTestResult("module", "package", "class", "testName", TestResultStatus.PASSED, 1l, 2l, null, null));
		container = new TestResultContainer(testResults.iterator(), new ResultFields());
	}

	@Test
	public void testNonEmptySubType() throws Exception {
		MatrixProject matrixProject = jenkins.createProject(MatrixProject.class, "matrix-project");

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
		xmlWriter.writeResults(container);
		xmlWriter.close();

		TestResultIterator iterator = new TestResultIterable(new File(testXml.getRemote())).iterator();
		Assert.assertEquals(buildType, iterator.getJobId());
		Assert.assertEquals(subType, iterator.getSubType());
	}
}
