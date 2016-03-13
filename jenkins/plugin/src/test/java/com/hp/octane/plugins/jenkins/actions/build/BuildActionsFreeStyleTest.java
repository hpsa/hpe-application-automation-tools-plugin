package com.hp.octane.plugins.jenkins.actions.build;

import com.gargoylesoftware.htmlunit.Page;
import com.hp.nga.integrations.dto.DTOFactory;
import com.hp.nga.integrations.dto.causes.CIEventCauseType;
import com.hp.nga.integrations.dto.parameters.CIParameter;
import com.hp.nga.integrations.dto.parameters.CIParameterType;
import com.hp.nga.integrations.dto.snapshots.SnapshotNode;
import com.hp.nga.integrations.dto.snapshots.CIBuildResult;
import com.hp.nga.integrations.dto.snapshots.CIBuildStatus;
import com.hp.octane.plugins.jenkins.actions.Utils;
import hudson.model.*;
import hudson.plugins.parameterizedtrigger.*;
import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule;

import java.io.IOException;
import java.util.Arrays;

import static org.junit.Assert.*;

/**
 * Created with IntelliJ IDEA.
 * User: gullery
 * Date: 13/01/15
 * Time: 11:46
 * To change this template use File | Settings | File Templates.
 */

public class BuildActionsFreeStyleTest {
	private static final DTOFactory dtoFactory = DTOFactory.getInstance();
	private static final String projectName = "root-job";
	private static final int MAX_RUN_WAITING_SECS = 20;

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
		int retries = 0;
		FreeStyleProject p = rule.createFreeStyleProject(projectName);

		JenkinsRule.WebClient client = rule.createWebClient();
		Page page;
		SnapshotNode snapshot;

		assertEquals(p.getBuilds().toArray().length, 0);
		Utils.buildProject(client, p);
		while ((p.getLastBuild() == null || p.getLastBuild().isBuilding()) && ++retries < MAX_RUN_WAITING_SECS) {
			Thread.sleep(1000);
		}
		assertEquals(p.getBuilds().toArray().length, 1);

		page = client.goTo("nga/api/v1/jobs/" + projectName + "/builds/" + p.getLastBuild().getNumber(), "application/json");
		snapshot = dtoFactory.dtoFromJson(page.getWebResponse().getContentAsString(), SnapshotNode.class);
		assertEquals(projectName, snapshot.getCiId());
		assertEquals(projectName, snapshot.getName());
		assertEquals(0, snapshot.getParameters().size());
		assertEquals(0, snapshot.getPhasesInternal().size());
		assertEquals(0, snapshot.getPhasesPostBuild().size());
		assertEquals(1, snapshot.getCauses().length);
		assertEquals(CIEventCauseType.USER, snapshot.getCauses()[0].getType());
		assertEquals(p.getLastBuild().getNumber(), (int) snapshot.getNumber());
		assertEquals(CIBuildStatus.FINISHED, snapshot.getStatus());
		assertEquals(CIBuildResult.SUCCESS, snapshot.getResult());
		assertNotNull(snapshot.getStartTime());
		assertNotNull(snapshot.getDuration());
		assertNotNull(snapshot.getEstimatedDuration());
	}

	//  Snapshot: free-style, with params, no children
	//
	@Test
	public void testFreeStyleWithParamsNoChildren() throws Exception {
		int retries = 0;
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
		SnapshotNode snapshot;
		CIParameter tmpParam;

		assertEquals(p.getBuilds().toArray().length, 0);
		Utils.buildProjectWithParams(client, p, "ParamA=false&ParamD=two&ParamX=some_string");
		while ((p.getLastBuild() == null || p.getLastBuild().isBuilding()) && ++retries < MAX_RUN_WAITING_SECS) {
			Thread.sleep(1000);
		}
		assertEquals(p.getBuilds().toArray().length, 1);

		page = client.goTo("nga/api/v1/jobs/" + projectName + "/builds/" + p.getLastBuild().getNumber(), "application/json");
		snapshot = dtoFactory.dtoFromJson(page.getWebResponse().getContentAsString(), SnapshotNode.class);
		assertEquals(projectName, snapshot.getCiId());
		assertEquals(projectName, snapshot.getName());
		assertEquals(5, snapshot.getParameters().size());
		assertEquals(0, snapshot.getPhasesInternal().size());
		assertEquals(0, snapshot.getPhasesPostBuild().size());
		assertEquals(1, snapshot.getCauses().length);
		assertEquals(CIEventCauseType.USER, snapshot.getCauses()[0].getType());
		assertEquals(p.getLastBuild().getNumber(), (int) snapshot.getNumber());
		assertEquals(CIBuildStatus.FINISHED, snapshot.getStatus());
		assertEquals(CIBuildResult.SUCCESS, snapshot.getResult());
		assertNotNull(snapshot.getStartTime());
		assertNotNull(snapshot.getDuration());
		assertNotNull(snapshot.getEstimatedDuration());

		tmpParam = snapshot.getParameters().get(0);
		assertEquals("ParamA", tmpParam.getName());
		assertEquals(CIParameterType.BOOLEAN, tmpParam.getType());
		assertEquals("bool", tmpParam.getDescription());
		assertEquals(true, tmpParam.getDefaultValue());
		assertEquals("false", tmpParam.getValue());
		assertNull(tmpParam.getChoices());

		tmpParam = snapshot.getParameters().get(1);
		assertEquals("ParamB", tmpParam.getName());
		assertEquals(CIParameterType.STRING, tmpParam.getType());
		assertEquals("string", tmpParam.getDescription());
		assertEquals("str", tmpParam.getDefaultValue());
		assertEquals("str", tmpParam.getValue());
		assertNull(tmpParam.getChoices());

		tmpParam = snapshot.getParameters().get(2);
		assertEquals("ParamC", tmpParam.getName());
		assertEquals(CIParameterType.STRING, tmpParam.getType());
		assertEquals("text", tmpParam.getDescription());
		assertEquals("txt", tmpParam.getDefaultValue());
		assertEquals("txt", tmpParam.getValue());

		tmpParam = snapshot.getParameters().get(3);
		assertEquals("ParamD", tmpParam.getName());
		assertEquals(CIParameterType.STRING, tmpParam.getType());
		assertEquals("choice", tmpParam.getDescription());
		assertEquals("one", tmpParam.getDefaultValue());
		assertEquals("two", tmpParam.getValue());

		tmpParam = snapshot.getParameters().get(4);
		assertEquals("ParamE", tmpParam.getName());
		assertEquals(CIParameterType.FILE, tmpParam.getType());
		assertEquals("file param", tmpParam.getDescription());
		assertEquals("", tmpParam.getDefaultValue());
		assertEquals(null, tmpParam.getValue());
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
		SnapshotNode snapshot;

		assertEquals(p.getBuilds().toArray().length, 0);
		Utils.buildProjectWithParams(client, p, "ParamA=false&ParamC=not_exists");
		while (lastToBeBuilt.getLastBuild() == null ||
				lastToBeBuilt.getLastBuild().getNumber() < 6 ||
				lastToBeBuilt.getLastBuild().isBuilding()) {
			Thread.sleep(100);
		}
		assertEquals(p.getBuilds().toArray().length, 1);

		page = client.goTo("nga/api/v1/jobs/" + projectName + "/builds/" + p.getLastBuild().getNumber(), "application/json");
		snapshot = dtoFactory.dtoFromJson(page.getWebResponse().getContentAsString(), SnapshotNode.class);
		assertEquals(projectName, snapshot.getCiId());
		assertEquals(projectName, snapshot.getName());
		assertEquals(2, snapshot.getParameters().size());
		assertEquals(2, snapshot.getPhasesInternal().size());
		assertEquals(2, snapshot.getPhasesPostBuild().size());
		assertEquals(1, snapshot.getCauses().length);
		assertEquals(CIEventCauseType.USER, snapshot.getCauses()[0].getType());
		assertEquals(p.getLastBuild().getNumber(), (int) snapshot.getNumber());
		assertEquals(CIBuildStatus.FINISHED, snapshot.getStatus());
		assertEquals(CIBuildResult.SUCCESS, snapshot.getResult());
		assertNotNull(snapshot.getStartTime());
		assertNotNull(snapshot.getDuration());
		assertNotNull(snapshot.getEstimatedDuration());
	}
}
