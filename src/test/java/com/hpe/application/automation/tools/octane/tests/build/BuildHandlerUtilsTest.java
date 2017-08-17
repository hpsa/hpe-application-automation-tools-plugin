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

package com.hpe.application.automation.tools.octane.tests.build;

import com.hpe.application.automation.tools.octane.tests.CopyResourceSCM;
import com.hpe.application.automation.tools.octane.tests.TestUtils;
import hudson.matrix.*;
import hudson.maven.MavenModuleSet;
import hudson.maven.MavenModuleSetBuild;
import hudson.model.FreeStyleBuild;
import hudson.model.FreeStyleProject;
import hudson.tasks.Maven;
import org.junit.Assert;
import org.junit.ClassRule;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule;
import org.jvnet.hudson.test.ToolInstallations;

import java.util.HashMap;

import static org.jvnet.hudson.test.JenkinsRule.NO_PROPERTIES;

@SuppressWarnings({"squid:S2699","squid:S3658","squid:S2259","squid:S1872","squid:S2925","squid:S109","squid:S1607","squid:S2701"})
public class BuildHandlerUtilsTest {

	@ClassRule
	public static final JenkinsRule jenkins = new JenkinsRule();

	@Test
	public void testMatrixBuildType() throws Exception {
		MatrixProject matrixProject = jenkins.createProject(MatrixProject.class, "matrix-project");
		matrixProject.setAxes(new AxisList(new Axis("OS", "Linux", "Windows")));
		MatrixBuild build = (MatrixBuild) TestUtils.runAndCheckBuild(matrixProject);

		BuildDescriptor descriptor = BuildHandlerUtils.getBuildType(build);
		Assert.assertEquals("matrix-project", descriptor.getJobId());
		Assert.assertEquals("", descriptor.getSubType());

		Assert.assertEquals("matrix-project", BuildHandlerUtils.getProjectFullName(build));

		HashMap<String, String> expectedType = new HashMap<>();
		expectedType.put("OS=Linux", "matrix-project/OS=Linux");
		expectedType.put("OS=Windows", "matrix-project/OS=Windows");

		for (MatrixRun run : build.getExactRuns()) {
			descriptor = BuildHandlerUtils.getBuildType(run);
			Assert.assertEquals("matrix-project", descriptor.getJobId());
			String fullName = expectedType.remove(descriptor.getSubType());
			Assert.assertEquals(fullName, BuildHandlerUtils.getProjectFullName(run));
		}
		Assert.assertTrue(expectedType.isEmpty());
	}

	@Test
	public void testMavenBuildType() throws Exception {
		MavenModuleSet project = jenkins.createProject(MavenModuleSet.class, "maven-project");
		project.runHeadless();

		Maven.MavenInstallation mavenInstallation = ToolInstallations.configureMaven3();

		//Maven.MavenInstallation mavenInstallation = new Maven.MavenInstallation("default-system-maven", System.getenv("MAVEN_HOME"), JenkinsRule.NO_PROPERTIES);

		project.setMaven(mavenInstallation.getName());
		//project.setGoals("test -Dmaven.test.failure.ignore=true");
		project.setGoals(String.format("clean test --settings %s\\conf\\settings.xml -Dmaven.repo.local=%s\\m2-temp -Dmaven.test.failure.ignore=true",System.getenv("MAVEN_HOME"),System.getenv("TEMP")));
		project.setScm(new CopyResourceSCM("/helloWorldRoot"));
		MavenModuleSetBuild build = (MavenModuleSetBuild) TestUtils.runAndCheckBuild(project);

		BuildDescriptor descriptor = BuildHandlerUtils.getBuildType(build);
		Assert.assertEquals("maven-project", descriptor.getJobId());
		Assert.assertEquals("", descriptor.getSubType());

		Assert.assertEquals("maven-project", BuildHandlerUtils.getProjectFullName(build));
	}

	@Test
	public void testFallbackBuildType() throws Exception {
		FreeStyleProject project = jenkins.createFreeStyleProject("freestyle-project");
		FreeStyleBuild build = (FreeStyleBuild) TestUtils.runAndCheckBuild(project);
		BuildDescriptor descriptor = BuildHandlerUtils.getBuildType(build);
		Assert.assertEquals("freestyle-project", descriptor.getJobId());
		Assert.assertEquals("", descriptor.getSubType());
		Assert.assertEquals("freestyle-project", BuildHandlerUtils.getProjectFullName(build));
	}
}
