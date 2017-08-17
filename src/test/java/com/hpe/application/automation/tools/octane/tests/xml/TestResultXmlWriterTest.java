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

package com.hpe.application.automation.tools.octane.tests.xml;

import com.hpe.application.automation.tools.octane.tests.TestResultContainer;
import com.hpe.application.automation.tools.octane.tests.TestResultIterable;
import com.hpe.application.automation.tools.octane.tests.junit.JUnitTestResult;
import com.hpe.application.automation.tools.octane.tests.TestResultIterator;
import com.hpe.application.automation.tools.octane.tests.TestUtils;
import com.hpe.application.automation.tools.octane.tests.detection.ResultFields;
import com.hpe.application.automation.tools.octane.tests.junit.TestResultStatus;
import com.hpe.application.automation.tools.octane.tests.testResult.TestResult;
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

@SuppressWarnings({"squid:S2698","squid:S2699"})
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
