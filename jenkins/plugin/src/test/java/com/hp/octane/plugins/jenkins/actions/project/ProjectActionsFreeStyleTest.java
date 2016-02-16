package com.hp.octane.plugins.jenkins.actions.project;

import com.gargoylesoftware.htmlunit.Page;
import com.hp.nga.integrations.dto.parameters.ParameterType;
import hudson.matrix.MatrixProject;
import hudson.maven.MavenModuleSet;
import hudson.model.*;
import hudson.plugins.parameterizedtrigger.*;
import hudson.tasks.BuildTrigger;
import hudson.tasks.Fingerprinter;
import hudson.tasks.Shell;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.util.Arrays;

import static org.junit.Assert.*;

/**
 * Created with IntelliJ IDEA.
 * User: gullery
 * Date: 13/01/15
 * Time: 11:39
 * To change this template use File | Settings | File Templates.
 */

public class ProjectActionsFreeStyleTest {
	final private String projectName = "root-job";

	@Rule
	public final JenkinsRule rule = new JenkinsRule();

	//  Structure test: free-style, no params, no children
	//
	@Test
	public void testStructureFreeStyleNoParamsNoChildren() throws IOException, SAXException {
		rule.createFreeStyleProject(projectName);

		JenkinsRule.WebClient client = rule.createWebClient();
		Page page;
		JSONObject body;
		JSONArray tmpArray;

		page = client.goTo("nga/jobs/" + projectName, "application/json");

		body = new JSONObject(page.getWebResponse().getContentAsString());
		assertEquals(body.length(), 4);
		assertTrue(body.has("name"));
		assertEquals(body.getString("name"), projectName);

		assertTrue(body.has("parameters"));
		tmpArray = body.getJSONArray("parameters");
		assertEquals(tmpArray.length(), 0);

		assertTrue(body.has("phasesInternal"));
		tmpArray = body.getJSONArray("phasesInternal");
		assertEquals(tmpArray.length(), 0);

		assertTrue(body.has("phasesPostBuild"));
		tmpArray = body.getJSONArray("phasesPostBuild");
		assertEquals(tmpArray.length(), 0);
	}

	//  Structure test: free-style, with params, no children
	//
	@Test
	public void testStructureFreeStyleWithParamsNoChildren() throws IOException, SAXException {
		FreeStyleProject p = rule.createFreeStyleProject(projectName);
		ParametersDefinitionProperty params = new ParametersDefinitionProperty(Arrays.asList(
				(ParameterDefinition) new BooleanParameterDefinition("ParamA", true, "bool"),
				(ParameterDefinition) new StringParameterDefinition("ParamB", "str", "string"),
				(ParameterDefinition) new TextParameterDefinition("ParamC", "txt", "text"),
				(ParameterDefinition) new ChoiceParameterDefinition("ParamD", new String[]{"one", "two", "three"}, "choice"),
				(ParameterDefinition) new FileParameterDefinition("ParamE", "file param")
		));
		p.addProperty(params);

		JenkinsRule.WebClient client = rule.createWebClient();
		Page page;
		JSONObject body;
		JSONArray tmpArray;
		JSONObject tmpParam;

		page = client.goTo("nga/jobs/" + projectName, "application/json");

		body = new JSONObject(page.getWebResponse().getContentAsString());
		assertEquals(body.length(), 4);
		assertTrue(body.has("name"));
		assertEquals(body.getString("name"), projectName);

		assertTrue(body.has("parameters"));
		tmpArray = body.getJSONArray("parameters");
		assertEquals(tmpArray.length(), 5);

		tmpParam = tmpArray.getJSONObject(0);
		assertEquals(tmpParam.length(), 5);
		assertEquals(tmpParam.getString("name"), "ParamA");
		assertEquals(tmpParam.getString("type"), ParameterType.BOOLEAN.toString());
		assertEquals(tmpParam.getString("description"), "bool");
		assertEquals(tmpParam.getBoolean("defaultValue"), true);
		assertTrue(tmpParam.isNull("choices"));

		tmpParam = tmpArray.getJSONObject(1);
		assertEquals(tmpParam.length(), 5);
		assertEquals(tmpParam.getString("name"), "ParamB");
		assertEquals(tmpParam.getString("type"), ParameterType.STRING.toString());
		assertEquals(tmpParam.getString("description"), "string");
		assertEquals(tmpParam.getString("defaultValue"), "str");
		assertTrue(tmpParam.isNull("choices"));

		tmpParam = tmpArray.getJSONObject(2);
		assertEquals(tmpParam.length(), 5);
		assertEquals(tmpParam.getString("name"), "ParamC");
		assertEquals(tmpParam.getString("type"), ParameterType.STRING.toString());
		assertEquals(tmpParam.getString("description"), "text");
		assertEquals(tmpParam.getString("defaultValue"), "txt");
		assertTrue(tmpParam.isNull("choices"));

		tmpParam = tmpArray.getJSONObject(3);
		assertEquals(tmpParam.length(), 5);
		assertEquals(tmpParam.getString("name"), "ParamD");
		assertEquals(tmpParam.getString("type"), ParameterType.STRING.toString());
		assertEquals(tmpParam.getString("description"), "choice");
		assertEquals(tmpParam.getString("defaultValue"), "one");
		assertNotNull(tmpParam.get("choices"));
		assertEquals(tmpParam.getJSONArray("choices").length(), 3);
		assertEquals(tmpParam.getJSONArray("choices").get(0), "one");
		assertEquals(tmpParam.getJSONArray("choices").get(1), "two");
		assertEquals(tmpParam.getJSONArray("choices").get(2), "three");

		tmpParam = tmpArray.getJSONObject(4);
		assertEquals(tmpParam.length(), 5);
		assertEquals(tmpParam.getString("name"), "ParamE");
		assertEquals(tmpParam.getString("type"), ParameterType.FILE.toString());
		assertEquals(tmpParam.getString("description"), "file param");
		assertEquals(tmpParam.getString("defaultValue"), "");
		assertTrue(tmpParam.isNull("choices"));

		assertTrue(body.has("phasesInternal"));
		tmpArray = body.getJSONArray("phasesInternal");
		assertEquals(tmpArray.length(), 0);

		assertTrue(body.has("phasesPostBuild"));
		tmpArray = body.getJSONArray("phasesPostBuild");
		assertEquals(tmpArray.length(), 0);
	}

	//  Structure test: free-style, with params, with children
	//
	@Test
	public void testStructureFreeStyleWithParamsWithChildren() throws IOException, SAXException {
		FreeStyleProject p = rule.createFreeStyleProject(projectName);
		FreeStyleProject p1 = rule.createFreeStyleProject("jobA");
		MatrixProject p2 = rule.createMatrixProject("jobB");
		FreeStyleProject p3 = rule.createFreeStyleProject("jobC");
		MavenModuleSet p4 = rule.createMavenProject("jobD");
		CustomProject p5 = rule.getInstance().createProject(CustomProject.class, "jobE");
		ParametersDefinitionProperty params = new ParametersDefinitionProperty(Arrays.asList(
				(ParameterDefinition) new BooleanParameterDefinition("ParamA", true, "bool"),
				(ParameterDefinition) new StringParameterDefinition("ParamB", "str", "string")
		));
		p.addProperty(params);
		p.getBuildersList().add(new TriggerBuilder(Arrays.asList(
				new BlockableBuildTriggerConfig("jobA, jobB", new BlockingBehaviour(
						Result.FAILURE,
						Result.UNSTABLE,
						Result.FAILURE
				), Arrays.asList(new AbstractBuildParameters[0])),
				new BlockableBuildTriggerConfig("jobC,jobD", null, Arrays.asList(new AbstractBuildParameters[0]))
		)));
		p.getBuildersList().add(new Shell(""));
		p.getBuildersList().add(new TriggerBuilder(Arrays.asList(
				new BlockableBuildTriggerConfig("jobA, jobB, jobE", new BlockingBehaviour(
						Result.FAILURE,
						Result.UNSTABLE,
						Result.FAILURE
				), Arrays.asList(new AbstractBuildParameters[0])),
				new BlockableBuildTriggerConfig("jobC,jobD", null, Arrays.asList(new AbstractBuildParameters[0]))
		)));
		p.getPublishersList().add(new BuildTrigger("jobA, jobB", Result.SUCCESS));
		p.getPublishersList().add(new hudson.plugins.parameterizedtrigger.BuildTrigger(Arrays.asList(
				new BuildTriggerConfig("jobC,jobD", ResultCondition.ALWAYS, false, null)
		)));
		p.getPublishersList().add(new Fingerprinter(""));

		JenkinsRule.WebClient client = rule.createWebClient();
		Page page;
		JSONObject body;
		JSONArray tmpArray;
		JSONObject tmpParam;
		JSONObject tmpPhase;
		JSONArray tmpJobs;
		JSONObject tmpJob;

		page = client.goTo("nga/jobs/" + projectName, "application/json");

		body = new JSONObject(page.getWebResponse().getContentAsString());
		assertEquals(body.length(), 4);
		assertTrue(body.has("name"));
		assertEquals(body.getString("name"), projectName);

		assertTrue(body.has("parameters"));
		tmpArray = body.getJSONArray("parameters");
		assertEquals(tmpArray.length(), 2);

		tmpParam = tmpArray.getJSONObject(0);
		assertEquals(tmpParam.length(), 5);
		assertEquals(tmpParam.getString("name"), "ParamA");
		assertEquals(tmpParam.getString("type"), ParameterType.BOOLEAN.toString());
		assertEquals(tmpParam.getString("description"), "bool");
		assertEquals(tmpParam.getBoolean("defaultValue"), true);
		assertTrue(tmpParam.isNull("choices"));

		tmpParam = tmpArray.getJSONObject(1);
		assertEquals(tmpParam.length(), 5);
		assertEquals(tmpParam.getString("name"), "ParamB");
		assertEquals(tmpParam.getString("type"), ParameterType.STRING.toString());
		assertEquals(tmpParam.getString("description"), "string");
		assertEquals(tmpParam.getString("defaultValue"), "str");
		assertTrue(tmpParam.isNull("choices"));

		assertTrue(body.has("phasesInternal"));
		tmpArray = body.getJSONArray("phasesInternal");
		assertEquals(tmpArray.length(), 4);

		tmpPhase = tmpArray.getJSONObject(0);
		assertEquals(tmpPhase.length(), 3);
		assertEquals(tmpPhase.getString("name"), "");
		assertEquals(tmpPhase.getBoolean("blocking"), true);
		tmpJobs = tmpPhase.getJSONArray("jobs");
		assertEquals(tmpJobs.length(), 2);
		tmpJob = tmpJobs.getJSONObject(0);
		assertEquals(tmpJob.length(), 4);
		assertEquals(tmpJob.getString("name"), "jobA");
		assertEquals(tmpJob.getJSONArray("parameters").length(), 0);
		assertEquals(tmpJob.getJSONArray("phasesInternal").length(), 0);
		assertEquals(tmpJob.getJSONArray("phasesPostBuild").length(), 0);
		tmpJob = tmpJobs.getJSONObject(1);
		assertEquals(tmpJob.length(), 4);
		assertEquals(tmpJob.getString("name"), "jobB");
		assertEquals(tmpJob.getJSONArray("parameters").length(), 0);
		assertEquals(tmpJob.getJSONArray("phasesInternal").length(), 0);
		assertEquals(tmpJob.getJSONArray("phasesPostBuild").length(), 0);

		tmpPhase = tmpArray.getJSONObject(1);
		assertEquals(tmpPhase.length(), 3);
		assertEquals(tmpPhase.getString("name"), "");
		assertEquals(tmpPhase.getBoolean("blocking"), false);
		tmpJobs = tmpPhase.getJSONArray("jobs");
		assertEquals(tmpJobs.length(), 2);
		tmpJob = tmpJobs.getJSONObject(0);
		assertEquals(tmpJob.length(), 4);
		assertEquals(tmpJob.getString("name"), "jobC");
		assertEquals(tmpJob.getJSONArray("parameters").length(), 0);
		assertEquals(tmpJob.getJSONArray("phasesInternal").length(), 0);
		assertEquals(tmpJob.getJSONArray("phasesPostBuild").length(), 0);
		tmpJob = tmpJobs.getJSONObject(1);
		assertEquals(tmpJob.length(), 4);
		assertEquals(tmpJob.getString("name"), "jobD");
		assertEquals(tmpJob.getJSONArray("parameters").length(), 0);
		assertEquals(tmpJob.getJSONArray("phasesInternal").length(), 0);
		assertEquals(tmpJob.getJSONArray("phasesPostBuild").length(), 0);

		tmpPhase = tmpArray.getJSONObject(2);
		assertEquals(tmpPhase.length(), 3);
		assertEquals(tmpPhase.getString("name"), "");
		assertEquals(tmpPhase.getBoolean("blocking"), true);
		tmpJobs = tmpPhase.getJSONArray("jobs");
		assertEquals(tmpJobs.length(), 3);
		tmpJob = tmpJobs.getJSONObject(0);
		assertEquals(tmpJob.length(), 4);
		assertEquals(tmpJob.getString("name"), "jobA");
		assertEquals(tmpJob.getJSONArray("parameters").length(), 0);
		assertEquals(tmpJob.getJSONArray("phasesInternal").length(), 0);
		assertEquals(tmpJob.getJSONArray("phasesPostBuild").length(), 0);
		tmpJob = tmpJobs.getJSONObject(1);
		assertEquals(tmpJob.length(), 4);
		assertEquals(tmpJob.getString("name"), "jobB");
		assertEquals(tmpJob.getJSONArray("parameters").length(), 0);
		assertEquals(tmpJob.getJSONArray("phasesInternal").length(), 0);
		assertEquals(tmpJob.getJSONArray("phasesPostBuild").length(), 0);
		tmpJob = tmpJobs.getJSONObject(2);
		assertEquals(tmpJob.length(), 4);
		assertEquals(tmpJob.getString("name"), "jobE");
		assertEquals(tmpJob.getJSONArray("parameters").length(), 0);
		assertEquals(tmpJob.getJSONArray("phasesInternal").length(), 0);
		assertEquals(tmpJob.getJSONArray("phasesPostBuild").length(), 0);

		tmpPhase = tmpArray.getJSONObject(3);
		assertEquals(tmpPhase.length(), 3);
		assertEquals(tmpPhase.getString("name"), "");
		assertEquals(tmpPhase.getBoolean("blocking"), false);
		tmpJobs = tmpPhase.getJSONArray("jobs");
		assertEquals(tmpJobs.length(), 2);
		tmpJob = tmpJobs.getJSONObject(0);
		assertEquals(tmpJob.length(), 4);
		assertEquals(tmpJob.getString("name"), "jobC");
		assertEquals(tmpJob.getJSONArray("parameters").length(), 0);
		assertEquals(tmpJob.getJSONArray("phasesInternal").length(), 0);
		assertEquals(tmpJob.getJSONArray("phasesPostBuild").length(), 0);
		tmpJob = tmpJobs.getJSONObject(1);
		assertEquals(tmpJob.length(), 4);
		assertEquals(tmpJob.getString("name"), "jobD");
		assertEquals(tmpJob.getJSONArray("parameters").length(), 0);
		assertEquals(tmpJob.getJSONArray("phasesInternal").length(), 0);
		assertEquals(tmpJob.getJSONArray("phasesPostBuild").length(), 0);

		assertTrue(body.has("phasesPostBuild"));
		tmpArray = body.getJSONArray("phasesPostBuild");
		assertEquals(tmpArray.length(), 2);

		tmpPhase = tmpArray.getJSONObject(0);
		assertEquals(tmpPhase.length(), 3);
		assertEquals(tmpPhase.getString("name"), "downstream");
		assertEquals(tmpPhase.getBoolean("blocking"), false);
		tmpJobs = tmpPhase.getJSONArray("jobs");
		assertEquals(tmpJobs.length(), 2);
		tmpJob = tmpJobs.getJSONObject(0);
		assertEquals(tmpJob.length(), 4);
		assertEquals(tmpJob.getString("name"), "jobA");
		assertEquals(tmpJob.getJSONArray("parameters").length(), 0);
		assertEquals(tmpJob.getJSONArray("phasesInternal").length(), 0);
		assertEquals(tmpJob.getJSONArray("phasesPostBuild").length(), 0);
		tmpJob = tmpJobs.getJSONObject(1);
		assertEquals(tmpJob.length(), 4);
		assertEquals(tmpJob.getString("name"), "jobB");
		assertEquals(tmpJob.getJSONArray("parameters").length(), 0);
		assertEquals(tmpJob.getJSONArray("phasesInternal").length(), 0);
		assertEquals(tmpJob.getJSONArray("phasesPostBuild").length(), 0);

		tmpPhase = tmpArray.getJSONObject(1);
		assertEquals(tmpPhase.length(), 3);
		assertEquals(tmpPhase.getString("name"), "");
		assertEquals(tmpPhase.getBoolean("blocking"), false);
		tmpJobs = tmpPhase.getJSONArray("jobs");
		assertEquals(tmpJobs.length(), 2);
		tmpJob = tmpJobs.getJSONObject(0);
		assertEquals(tmpJob.length(), 4);
		assertEquals(tmpJob.getString("name"), "jobC");
		assertEquals(tmpJob.getJSONArray("parameters").length(), 0);
		assertEquals(tmpJob.getJSONArray("phasesInternal").length(), 0);
		assertEquals(tmpJob.getJSONArray("phasesPostBuild").length(), 0);
		tmpJob = tmpJobs.getJSONObject(1);
		assertEquals(tmpJob.length(), 4);
		assertEquals(tmpJob.getString("name"), "jobD");
		assertEquals(tmpJob.getJSONArray("parameters").length(), 0);
		assertEquals(tmpJob.getJSONArray("phasesInternal").length(), 0);
		assertEquals(tmpJob.getJSONArray("phasesPostBuild").length(), 0);
	}
}
