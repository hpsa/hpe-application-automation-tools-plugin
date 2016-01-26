package com.hp.octane.plugins.jenkins.actions.build;

import com.gargoylesoftware.htmlunit.Page;
import com.hp.nga.integrations.dto.snapshots.SnapshotResult;
import com.hp.nga.integrations.dto.snapshots.SnapshotStatus;
import com.hp.octane.plugins.jenkins.actions.Utils;
import com.hp.octane.plugins.jenkins.model.parameters.ParameterType;
import hudson.model.*;
import hudson.plugins.parameterizedtrigger.*;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule;

import java.io.IOException;
import java.util.Arrays;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Created with IntelliJ IDEA.
 * User: gullery
 * Date: 13/01/15
 * Time: 11:46
 * To change this template use File | Settings | File Templates.
 */

public class BuildActionsFreeStyleTest {
	final private String projectName = "root-job";

	@Rule
	public final JenkinsRule rule = new JenkinsRule();

	private void createProjectStructure(FreeStyleProject project) throws IOException {
		FreeStyleProject jobA = rule.createFreeStyleProject("jobA");
		FreeStyleProject jobB = rule.createFreeStyleProject("jobB");
		FreeStyleProject jobC = rule.createFreeStyleProject("jobC");
		FreeStyleProject jobAA = rule.createFreeStyleProject("jobAA");
		FreeStyleProject jobBB = rule.createFreeStyleProject("jobBB");
		FreeStyleProject jobCC = rule.createFreeStyleProject("jobCC");

		//  jobA
		jobA.getBuildersList().add(Utils.getSleepScript(5));
		jobA.getBuildersList().add(new TriggerBuilder(Arrays.asList(
				new BlockableBuildTriggerConfig("jobAA, jobC", new BlockingBehaviour(
						Result.FAILURE,
						Result.FAILURE,
						Result.UNSTABLE
				), null)
		)));

		//  jobB
		jobB.getBuildersList().add(Utils.getSleepScript(12));
		jobB.getPublishersList().add(new hudson.tasks.BuildTrigger("jobBB, jobC", Result.SUCCESS));

		//  jobC
		jobC.getBuildersList().add(Utils.getSleepScript(20));
		jobC.getPublishersList().add(new hudson.plugins.parameterizedtrigger.BuildTrigger(Arrays.asList(
				new BuildTriggerConfig("jobCC", ResultCondition.ALWAYS, true, null)
		)));

		jobAA.getBuildersList().add(Utils.getSleepScript(10));
		jobBB.getBuildersList().add(Utils.getSleepScript(25));
		jobCC.getBuildersList().add(Utils.getSleepScript(17));

		//  root job config
		project.getBuildersList().add(new TriggerBuilder(Arrays.asList(
				new BlockableBuildTriggerConfig("jobA, jobB", new BlockingBehaviour(
						Result.FAILURE,
						Result.FAILURE,
						Result.UNSTABLE
				), Arrays.asList(new AbstractBuildParameters[0])),
				new BlockableBuildTriggerConfig("jobC", new BlockingBehaviour(
						Result.FAILURE,
						Result.FAILURE,
						Result.UNSTABLE
				), Arrays.asList(new AbstractBuildParameters[0]))
		)));
		project.getPublishersList().add(new hudson.tasks.BuildTrigger("jobA, jobB", Result.SUCCESS));
		project.getPublishersList().add(new hudson.plugins.parameterizedtrigger.BuildTrigger(Arrays.asList(
				new BuildTriggerConfig("jobC", ResultCondition.ALWAYS, true, null)
		)));
	}

	//  Snapshot: free-style, no params, no children
	//
	@Test
	public void testFreeStyleNoParamsNoChildren() throws Exception {
		FreeStyleProject p = rule.createFreeStyleProject(projectName);

		JenkinsRule.WebClient client = rule.createWebClient();
		Page page;
		JSONObject body;
		JSONObject tmpObject;
		JSONArray tmpArray;

		assertEquals(p.getBuilds().toArray().length, 0);
		Utils.buildProject(client, p);
		while (p.getLastBuild() == null || p.getLastBuild().isBuilding()) {
			Thread.sleep(1000);
		}
		assertEquals(p.getBuilds().toArray().length, 1);

		page = client.goTo("job/" + projectName + "/" + p.getLastBuild().getNumber() + "/octane/snapshot", "application/json");
		body = new JSONObject(page.getWebResponse().getContentAsString());
		assertEquals(body.length(), 12);
		assertEquals(body.getString("name"), projectName);

		assertFalse(body.isNull("parameters"));
		tmpArray = body.getJSONArray("parameters");
		assertEquals(tmpArray.length(), 0);

		assertFalse(body.isNull("phasesInternal"));
		tmpArray = body.getJSONArray("phasesInternal");
		assertEquals(tmpArray.length(), 0);

		assertFalse(body.isNull("phasesPostBuild"));
		tmpArray = body.getJSONArray("phasesPostBuild");
		assertEquals(tmpArray.length(), 0);

		assertFalse(body.isNull("causes"));
		tmpArray = body.getJSONArray("causes");
		assertEquals(tmpArray.length(), 1);
		tmpObject = tmpArray.getJSONObject(0);
		assertEquals(tmpObject.getString("type"), "user");

		assertFalse(body.isNull("duration"));
		assertFalse(body.isNull("estimatedDuration"));
		assertEquals(body.getInt("number"), p.getLastBuild().getNumber());
		assertEquals(body.getString("result"), SnapshotResult.SUCCESS.toString());
		assertTrue(body.has("scmData"));
		assertFalse(body.isNull("startTime"));
		assertEquals(body.getString("status"), SnapshotStatus.FINISHED.toString());
	}

	//  Snapshot: free-style, with params, no children
	//
	@Test
	public void testFreeStyleWithParamsNoChildren() throws Exception {
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
		JSONObject tmpObject;
		JSONArray tmpArray;

		assertEquals(p.getBuilds().toArray().length, 0);
		Utils.buildProjectWithParams(client, p, "ParamA=false&ParamD=two&ParamX=some_string");
		while (p.getLastBuild() == null || p.getLastBuild().isBuilding()) {
			Thread.sleep(1000);
		}
		assertEquals(p.getBuilds().toArray().length, 1);

		page = client.goTo("job/" + projectName + "/" + p.getLastBuild().getNumber() + "/octane/snapshot", "application/json");
		body = new JSONObject(page.getWebResponse().getContentAsString());
		assertEquals(body.length(), 12);
		assertEquals(body.getString("name"), projectName);
		tmpArray = body.getJSONArray("parameters");
		assertEquals(tmpArray.length(), 5);

		tmpObject = tmpArray.getJSONObject(0);
		assertEquals(tmpObject.getString("name"), "ParamA");
		assertEquals(tmpObject.getString("type"), ParameterType.BOOLEAN.toString());
		assertEquals(tmpObject.getString("description"), "bool");
		assertEquals(tmpObject.getBoolean("defaultValue"), true);
		assertEquals(tmpObject.getBoolean("value"), false);

		tmpObject = tmpArray.getJSONObject(1);
		assertEquals(tmpObject.getString("name"), "ParamB");
		assertEquals(tmpObject.getString("type"), ParameterType.STRING.toString());
		assertEquals(tmpObject.getString("description"), "string");
		assertEquals(tmpObject.getString("defaultValue"), "str");
		assertEquals(tmpObject.getString("value"), "str");

		tmpObject = tmpArray.getJSONObject(2);
		assertEquals(tmpObject.getString("name"), "ParamC");
		assertEquals(tmpObject.getString("type"), ParameterType.STRING.toString());
		assertEquals(tmpObject.getString("description"), "text");
		assertEquals(tmpObject.getString("defaultValue"), "txt");
		assertEquals(tmpObject.getString("value"), "txt");

		tmpObject = tmpArray.getJSONObject(3);
		assertEquals(tmpObject.getString("name"), "ParamD");
		assertEquals(tmpObject.getString("type"), ParameterType.STRING.toString());
		assertEquals(tmpObject.getString("description"), "choice");
		assertEquals(tmpObject.getString("defaultValue"), "one");
		assertEquals(tmpObject.getString("value"), "two");

		tmpObject = tmpArray.getJSONObject(4);
		assertEquals(tmpObject.getString("name"), "ParamE");
		assertEquals(tmpObject.getString("type"), ParameterType.FILE.toString());
		assertEquals(tmpObject.getString("description"), "file param");
		assertEquals(tmpObject.getString("defaultValue"), "");
		assertTrue(tmpObject.isNull("value"));
	}

	//  Snapshot: free-style, with params, with children
	//
	@Test
	public void testFreeStyleWithParamsWithChildren() throws Exception {
		rule.jenkins.setNumExecutors(10);
		rule.jenkins.setNodes(rule.jenkins.getNodes());
		FreeStyleProject p = rule.createFreeStyleProject(projectName);
		createProjectStructure(p);
		FreeStyleProject lastToBeBuilt = (FreeStyleProject) rule.jenkins.getItem("jobCC");

		ParametersDefinitionProperty params = new ParametersDefinitionProperty(Arrays.asList(
				(ParameterDefinition) new BooleanParameterDefinition("ParamA", true, "bool"),
				(ParameterDefinition) new StringParameterDefinition("ParamB", "str", "string")
		));
		p.addProperty(params);

		JenkinsRule.WebClient client = rule.createWebClient();
		Page page;
		JSONObject body;
		JSONObject tmpObject;
		JSONArray tmpArray;

		assertEquals(p.getBuilds().toArray().length, 0);
		Utils.buildProjectWithParams(client, p, "ParamA=false&ParamC=not_exists");
		while (lastToBeBuilt.getLastBuild() == null ||
				lastToBeBuilt.getLastBuild().getNumber() < 6 ||
				lastToBeBuilt.getLastBuild().isBuilding()) {
			Thread.sleep(100);
		}
		assertEquals(p.getBuilds().toArray().length, 1);

		page = client.goTo("job/" + projectName + "/" + p.getLastBuild().getNumber() + "/octane/snapshot", "application/json");
		body = new JSONObject(page.getWebResponse().getContentAsString());
		assertEquals(body.length(), 12);
		assertEquals(body.getString("name"), projectName);
		tmpArray = body.getJSONArray("parameters");
		assertEquals(tmpArray.length(), 2);

		//  internals
		tmpArray = body.getJSONArray("phasesInternal");
		assertEquals(tmpArray.length(), 2);

		//  post builds
		tmpArray = body.getJSONArray("phasesPostBuild");
		assertEquals(tmpArray.length(), 2);
	}
}
