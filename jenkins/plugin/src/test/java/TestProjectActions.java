import com.gargoylesoftware.htmlunit.Page;
import com.hp.octane.plugins.jenkins.actions.ProjectActions;
import com.hp.octane.plugins.jenkins.model.pipeline.ParameterType;
import com.tikal.jenkins.plugins.multijob.MultiJobBuilder;
import com.tikal.jenkins.plugins.multijob.MultiJobProject;
import com.tikal.jenkins.plugins.multijob.PhaseJobsConfig;
import hudson.matrix.MatrixProject;
import hudson.maven.MavenModuleSet;
import hudson.model.*;
import hudson.plugins.parameterizedtrigger.*;
import hudson.tasks.*;
import hudson.tasks.BuildTrigger;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Created with IntelliJ IDEA.
 * User: gullery
 * Date: 08/01/15
 * Time: 12:56
 * To change this template use File | Settings | File Templates.
 */

public class TestProjectActions {
	final private String projectName = "free-style-test";

	@Rule
	public JenkinsRule rule = new JenkinsRule();

	@Test
	public void testOctaneActionAdded() throws IOException {
		boolean actionFound = false;
		FreeStyleProject p = rule.createFreeStyleProject(projectName);
		List<Action> actions = p.getActions();
		for (Action action : actions)
			if (action.getClass() == ProjectActions.OctaneProjectActions.class) {
				actionFound = true;
				break;
			}
		assertTrue(actionFound);
	}

	@Test
	public void testOctaneActionsClass() throws IOException {
		FreeStyleProject p = rule.createFreeStyleProject(projectName);
		ProjectActions.OctaneProjectActions octaneActions = new ProjectActions.OctaneProjectActions(p);
		assertEquals(octaneActions.getIconFileName(), null);
		assertEquals(octaneActions.getDisplayName(), null);
		assertEquals(octaneActions.getUrlName(), "octane");
	}

	//  Structure test - free-style, no params, no children
	//
	@Test
	public void testStructureFreeStyleNoParamsNoChildren() throws IOException, SAXException {
		rule.createFreeStyleProject(projectName);

		JenkinsRule.WebClient client = rule.createWebClient();
		Page page;
		JSONObject body;
		JSONArray tmpArray;

		page = client.goTo("job/" + projectName + "/octane/structure", "application/json");
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

	//  Structure test - free-style, with params, no children
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

		page = client.goTo("job/" + projectName + "/octane/structure", "application/json");
		body = new JSONObject(page.getWebResponse().getContentAsString());
		assertEquals(body.length(), 4);
		assertTrue(body.has("name"));
		assertEquals(body.getString("name"), projectName);

		assertTrue(body.has("parameters"));
		tmpArray = body.getJSONArray("parameters");
		assertEquals(tmpArray.length(), 5);

		tmpParam = tmpArray.getJSONObject(0);
		assertEquals(tmpParam.length(), 4);
		assertEquals(tmpParam.getString("name"), "ParamA");
		assertEquals(tmpParam.getString("type"), ParameterType.BOOLEAN.toString());
		assertEquals(tmpParam.getString("description"), "bool");
		assertEquals(tmpParam.getBoolean("defaultValue"), true);

		tmpParam = tmpArray.getJSONObject(1);
		assertEquals(tmpParam.length(), 4);
		assertEquals(tmpParam.getString("name"), "ParamB");
		assertEquals(tmpParam.getString("type"), ParameterType.STRING.toString());
		assertEquals(tmpParam.getString("description"), "string");
		assertEquals(tmpParam.getString("defaultValue"), "str");

		tmpParam = tmpArray.getJSONObject(2);
		assertEquals(tmpParam.length(), 4);
		assertEquals(tmpParam.getString("name"), "ParamC");
		assertEquals(tmpParam.getString("type"), ParameterType.STRING.toString());
		assertEquals(tmpParam.getString("description"), "text");
		assertEquals(tmpParam.getString("defaultValue"), "txt");

		tmpParam = tmpArray.getJSONObject(3);
		assertEquals(tmpParam.length(), 4);
		assertEquals(tmpParam.getString("name"), "ParamD");
		assertEquals(tmpParam.getString("type"), ParameterType.STRING.toString());
		assertEquals(tmpParam.getString("description"), "choice");
		assertEquals(tmpParam.getString("defaultValue"), "one");

		tmpParam = tmpArray.getJSONObject(4);
		assertEquals(tmpParam.length(), 4);
		assertEquals(tmpParam.getString("name"), "ParamE");
		assertEquals(tmpParam.getString("type"), ParameterType.UNAVAILABLE.toString());
		assertEquals(tmpParam.getString("description"), "file param");
		assertTrue(tmpParam.isNull("defaultValue"));

		assertTrue(body.has("phasesInternal"));
		tmpArray = body.getJSONArray("phasesInternal");
		assertEquals(tmpArray.length(), 0);

		assertTrue(body.has("phasesPostBuild"));
		tmpArray = body.getJSONArray("phasesPostBuild");
		assertEquals(tmpArray.length(), 0);
	}

	//  Structure test - free-style, with params, with children
	//
	@Test
	public void testStructureFreeStyleWithParamsWithChildren() throws IOException, SAXException {
		FreeStyleProject p = rule.createFreeStyleProject(projectName);
		FreeStyleProject p1 = rule.createFreeStyleProject("jobA");
		MatrixProject p2 = rule.createMatrixProject("jobB");
		FreeStyleProject p3 = rule.createFreeStyleProject("jobC");
		MavenModuleSet p4 = rule.createMavenProject("jobD");
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
				new BlockableBuildTriggerConfig("jobA, jobB", new BlockingBehaviour(
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

		page = client.goTo("job/" + projectName + "/octane/structure", "application/json");
		body = new JSONObject(page.getWebResponse().getContentAsString());
		assertEquals(body.length(), 4);
		assertTrue(body.has("name"));
		assertEquals(body.getString("name"), projectName);

		assertTrue(body.has("parameters"));
		tmpArray = body.getJSONArray("parameters");
		assertEquals(tmpArray.length(), 2);

		tmpParam = tmpArray.getJSONObject(0);
		assertEquals(tmpParam.length(), 4);
		assertEquals(tmpParam.getString("name"), "ParamA");
		assertEquals(tmpParam.getString("type"), ParameterType.BOOLEAN.toString());
		assertEquals(tmpParam.getString("description"), "bool");
		assertEquals(tmpParam.getBoolean("defaultValue"), true);

		tmpParam = tmpArray.getJSONObject(1);
		assertEquals(tmpParam.length(), 4);
		assertEquals(tmpParam.getString("name"), "ParamB");
		assertEquals(tmpParam.getString("type"), ParameterType.STRING.toString());
		assertEquals(tmpParam.getString("description"), "string");
		assertEquals(tmpParam.getString("defaultValue"), "str");

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

	//  Structure test - matrix, no params, no children
	//
	@Test
	public void testStructureMatrixNoParamsNoChildren() throws IOException, SAXException {
		rule.createMatrixProject(projectName);

		JenkinsRule.WebClient client = rule.createWebClient();
		Page page;
		JSONObject body;
		JSONArray tmpArray;

		page = client.goTo("job/" + projectName + "/octane/structure", "application/json");
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

	//  Structure test - matrix, with params, no children
	//
	@Test
	public void testStructureMatrixWithParamsNoChildren() throws IOException, SAXException {
		MatrixProject p = rule.createMatrixProject(projectName);
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

		page = client.goTo("job/" + projectName + "/octane/structure", "application/json");
		body = new JSONObject(page.getWebResponse().getContentAsString());
		assertEquals(body.length(), 4);
		assertTrue(body.has("name"));
		assertEquals(body.getString("name"), projectName);

		assertTrue(body.has("parameters"));
		tmpArray = body.getJSONArray("parameters");
		assertEquals(tmpArray.length(), 5);

		tmpParam = tmpArray.getJSONObject(0);
		assertEquals(tmpParam.length(), 4);
		assertEquals(tmpParam.getString("name"), "ParamA");
		assertEquals(tmpParam.getString("type"), ParameterType.BOOLEAN.toString());
		assertEquals(tmpParam.getString("description"), "bool");
		assertEquals(tmpParam.getBoolean("defaultValue"), true);

		tmpParam = tmpArray.getJSONObject(1);
		assertEquals(tmpParam.length(), 4);
		assertEquals(tmpParam.getString("name"), "ParamB");
		assertEquals(tmpParam.getString("type"), ParameterType.STRING.toString());
		assertEquals(tmpParam.getString("description"), "string");
		assertEquals(tmpParam.getString("defaultValue"), "str");

		tmpParam = tmpArray.getJSONObject(2);
		assertEquals(tmpParam.length(), 4);
		assertEquals(tmpParam.getString("name"), "ParamC");
		assertEquals(tmpParam.getString("type"), ParameterType.STRING.toString());
		assertEquals(tmpParam.getString("description"), "text");
		assertEquals(tmpParam.getString("defaultValue"), "txt");

		tmpParam = tmpArray.getJSONObject(3);
		assertEquals(tmpParam.length(), 4);
		assertEquals(tmpParam.getString("name"), "ParamD");
		assertEquals(tmpParam.getString("type"), ParameterType.STRING.toString());
		assertEquals(tmpParam.getString("description"), "choice");
		assertEquals(tmpParam.getString("defaultValue"), "one");

		tmpParam = tmpArray.getJSONObject(4);
		assertEquals(tmpParam.length(), 4);
		assertEquals(tmpParam.getString("name"), "ParamE");
		assertEquals(tmpParam.getString("type"), ParameterType.UNAVAILABLE.toString());
		assertEquals(tmpParam.getString("description"), "file param");
		assertTrue(tmpParam.isNull("defaultValue"));

		assertTrue(body.has("phasesInternal"));
		tmpArray = body.getJSONArray("phasesInternal");
		assertEquals(tmpArray.length(), 0);

		assertTrue(body.has("phasesPostBuild"));
		tmpArray = body.getJSONArray("phasesPostBuild");
		assertEquals(tmpArray.length(), 0);
	}

	//  Structure test - matrix, with params, with children
	//
	@Test
	public void testStructureMatrixWithParamsWithChildren() throws IOException, SAXException {
		MatrixProject p = rule.createMatrixProject(projectName);
		FreeStyleProject p1 = rule.createFreeStyleProject("jobA");
		MatrixProject p2 = rule.createMatrixProject("jobB");
		FreeStyleProject p3 = rule.createFreeStyleProject("jobC");
		MavenModuleSet p4 = rule.createMavenProject("jobD");
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
		p.getPublishersList().add(new Fingerprinter(""));
		p.getPublishersList().add(new BuildTrigger("jobA, jobB", Result.SUCCESS));
		p.getPublishersList().add(new hudson.plugins.parameterizedtrigger.BuildTrigger(Arrays.asList(
				new BuildTriggerConfig("jobC,jobD", ResultCondition.ALWAYS, false, null)
		)));

		JenkinsRule.WebClient client = rule.createWebClient();
		Page page;
		JSONObject body;
		JSONArray tmpArray;
		JSONObject tmpParam;
		JSONObject tmpPhase;
		JSONArray tmpJobs;
		JSONObject tmpJob;

		page = client.goTo("job/" + projectName + "/octane/structure", "application/json");
		body = new JSONObject(page.getWebResponse().getContentAsString());
		assertEquals(body.length(), 4);
		assertTrue(body.has("name"));
		assertEquals(body.getString("name"), projectName);

		assertTrue(body.has("parameters"));
		tmpArray = body.getJSONArray("parameters");
		assertEquals(tmpArray.length(), 2);

		tmpParam = tmpArray.getJSONObject(0);
		assertEquals(tmpParam.length(), 4);
		assertEquals(tmpParam.getString("name"), "ParamA");
		assertEquals(tmpParam.getString("type"), ParameterType.BOOLEAN.toString());
		assertEquals(tmpParam.getString("description"), "bool");
		assertEquals(tmpParam.getBoolean("defaultValue"), true);

		tmpParam = tmpArray.getJSONObject(1);
		assertEquals(tmpParam.length(), 4);
		assertEquals(tmpParam.getString("name"), "ParamB");
		assertEquals(tmpParam.getString("type"), ParameterType.STRING.toString());
		assertEquals(tmpParam.getString("description"), "string");
		assertEquals(tmpParam.getString("defaultValue"), "str");

		assertTrue(body.has("phasesInternal"));
		tmpArray = body.getJSONArray("phasesInternal");
		assertEquals(tmpArray.length(), 2);

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

	//  Structure test - maven, no params, no children
	//
	@Test
	public void testStructureMavenNoParamsNoChildren() throws IOException, SAXException {
		rule.createMavenProject(projectName);

		JenkinsRule.WebClient client = rule.createWebClient();
		Page page;
		JSONObject body;
		JSONArray tmpArray;

		page = client.goTo("job/" + projectName + "/octane/structure", "application/json");
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

	//  Structure test - maven, with params, no children
	//
	@Test
	public void testStructureMavenWithParamsNoChildren() throws IOException, SAXException {
		MavenModuleSet p = rule.createMavenProject(projectName);
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

		page = client.goTo("job/" + projectName + "/octane/structure", "application/json");
		body = new JSONObject(page.getWebResponse().getContentAsString());
		assertEquals(body.length(), 4);
		assertTrue(body.has("name"));
		assertEquals(body.getString("name"), projectName);

		assertTrue(body.has("parameters"));
		tmpArray = body.getJSONArray("parameters");
		assertEquals(tmpArray.length(), 5);

		tmpParam = tmpArray.getJSONObject(0);
		assertEquals(tmpParam.length(), 4);
		assertEquals(tmpParam.getString("name"), "ParamA");
		assertEquals(tmpParam.getString("type"), ParameterType.BOOLEAN.toString());
		assertEquals(tmpParam.getString("description"), "bool");
		assertEquals(tmpParam.getBoolean("defaultValue"), true);

		tmpParam = tmpArray.getJSONObject(1);
		assertEquals(tmpParam.length(), 4);
		assertEquals(tmpParam.getString("name"), "ParamB");
		assertEquals(tmpParam.getString("type"), ParameterType.STRING.toString());
		assertEquals(tmpParam.getString("description"), "string");
		assertEquals(tmpParam.getString("defaultValue"), "str");

		tmpParam = tmpArray.getJSONObject(2);
		assertEquals(tmpParam.length(), 4);
		assertEquals(tmpParam.getString("name"), "ParamC");
		assertEquals(tmpParam.getString("type"), ParameterType.STRING.toString());
		assertEquals(tmpParam.getString("description"), "text");
		assertEquals(tmpParam.getString("defaultValue"), "txt");

		tmpParam = tmpArray.getJSONObject(3);
		assertEquals(tmpParam.length(), 4);
		assertEquals(tmpParam.getString("name"), "ParamD");
		assertEquals(tmpParam.getString("type"), ParameterType.STRING.toString());
		assertEquals(tmpParam.getString("description"), "choice");
		assertEquals(tmpParam.getString("defaultValue"), "one");

		tmpParam = tmpArray.getJSONObject(4);
		assertEquals(tmpParam.length(), 4);
		assertEquals(tmpParam.getString("name"), "ParamE");
		assertEquals(tmpParam.getString("type"), ParameterType.UNAVAILABLE.toString());
		assertEquals(tmpParam.getString("description"), "file param");
		assertTrue(tmpParam.isNull("defaultValue"));

		assertTrue(body.has("phasesInternal"));
		tmpArray = body.getJSONArray("phasesInternal");
		assertEquals(tmpArray.length(), 0);

		assertTrue(body.has("phasesPostBuild"));
		tmpArray = body.getJSONArray("phasesPostBuild");
		assertEquals(tmpArray.length(), 0);
	}

	//  Structure test - maven, with params, with children
	//
	@Test
	public void testStructureMavenWithParamsWithChildren() throws IOException, SAXException {
		MavenModuleSet p = rule.createMavenProject(projectName);
		FreeStyleProject p1 = rule.createFreeStyleProject("jobA");
		MatrixProject p2 = rule.createMatrixProject("jobB");
		FreeStyleProject p3 = rule.createFreeStyleProject("jobC");
		MatrixProject p4 = rule.createMatrixProject("jobD");
		ParametersDefinitionProperty params = new ParametersDefinitionProperty(Arrays.asList(
				(ParameterDefinition) new BooleanParameterDefinition("ParamA", true, "bool"),
				(ParameterDefinition) new StringParameterDefinition("ParamB", "str", "string")
		));
		p.addProperty(params);
		p.getPrebuilders().add(new TriggerBuilder(Arrays.asList(
				new BlockableBuildTriggerConfig("jobA, jobB", new BlockingBehaviour(
						Result.FAILURE,
						Result.UNSTABLE,
						Result.FAILURE
				), Arrays.asList(new AbstractBuildParameters[0])),
				new BlockableBuildTriggerConfig("jobC,jobD", null, Arrays.asList(new AbstractBuildParameters[0]))
		)));
		p.getPrebuilders().add(new Shell(""));
		p.getPostbuilders().add(new Shell(""));
		p.getPostbuilders().add(new TriggerBuilder(Arrays.asList(
				new BlockableBuildTriggerConfig("jobA, jobB", new BlockingBehaviour(
						Result.FAILURE,
						Result.UNSTABLE,
						Result.FAILURE
				), Arrays.asList(new AbstractBuildParameters[0])),
				new BlockableBuildTriggerConfig("jobC,jobD", null, Arrays.asList(new AbstractBuildParameters[0]))
		)));
		p.getPublishersList().add(new BuildTrigger("jobA, jobB", Result.SUCCESS));
		p.getPublishersList().add(new Fingerprinter(""));
		p.getPublishersList().add(new hudson.plugins.parameterizedtrigger.BuildTrigger(Arrays.asList(
				new BuildTriggerConfig("jobC,jobD", ResultCondition.ALWAYS, false, null)
		)));

		JenkinsRule.WebClient client = rule.createWebClient();
		Page page;
		JSONObject body;
		JSONArray tmpArray;
		JSONObject tmpParam;
		JSONObject tmpPhase;
		JSONArray tmpJobs;
		JSONObject tmpJob;

		page = client.goTo("job/" + projectName + "/octane/structure", "application/json");
		body = new JSONObject(page.getWebResponse().getContentAsString());
		assertEquals(body.length(), 4);
		assertTrue(body.has("name"));
		assertEquals(body.getString("name"), projectName);

		assertTrue(body.has("parameters"));
		tmpArray = body.getJSONArray("parameters");
		assertEquals(tmpArray.length(), 2);

		tmpParam = tmpArray.getJSONObject(0);
		assertEquals(tmpParam.length(), 4);
		assertEquals(tmpParam.getString("name"), "ParamA");
		assertEquals(tmpParam.getString("type"), ParameterType.BOOLEAN.toString());
		assertEquals(tmpParam.getString("description"), "bool");
		assertEquals(tmpParam.getBoolean("defaultValue"), true);

		tmpParam = tmpArray.getJSONObject(1);
		assertEquals(tmpParam.length(), 4);
		assertEquals(tmpParam.getString("name"), "ParamB");
		assertEquals(tmpParam.getString("type"), ParameterType.STRING.toString());
		assertEquals(tmpParam.getString("description"), "string");
		assertEquals(tmpParam.getString("defaultValue"), "str");

		assertTrue(body.has("phasesInternal"));
		tmpArray = body.getJSONArray("phasesInternal");
		assertEquals(tmpArray.length(), 4);

		tmpPhase = tmpArray.getJSONObject(0);
		assertEquals(tmpPhase.length(), 3);
		assertEquals(tmpPhase.getString("name"), "pre-maven");
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
		assertEquals(tmpPhase.getString("name"), "pre-maven");
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
		assertEquals(tmpPhase.getString("name"), "post-maven");
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

		tmpPhase = tmpArray.getJSONObject(3);
		assertEquals(tmpPhase.length(), 3);
		assertEquals(tmpPhase.getString("name"), "post-maven");
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

	//  Structure test - multi-job, no params, no children
	//
	@Test
	public void testStructureMultiJobNoParamsNoChildren() throws IOException, SAXException {
		rule.getInstance().createProject(MultiJobProject.class, projectName);

		JenkinsRule.WebClient client = rule.createWebClient();
		Page page;
		JSONObject body;
		JSONArray tmpArray;

		page = client.goTo("job/" + projectName + "/octane/structure", "application/json");
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

	//  Structure test - multi-job, with params, no children
	//
	@Test
	public void testStructureMultiJobWithParamsNoChildren() throws IOException, SAXException {
		MultiJobProject p = rule.getInstance().createProject(MultiJobProject.class, projectName);
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

		page = client.goTo("job/" + projectName + "/octane/structure", "application/json");
		body = new JSONObject(page.getWebResponse().getContentAsString());
		assertEquals(body.length(), 4);
		assertTrue(body.has("name"));
		assertEquals(body.getString("name"), projectName);

		assertTrue(body.has("parameters"));
		tmpArray = body.getJSONArray("parameters");
		assertEquals(tmpArray.length(), 5);

		tmpParam = tmpArray.getJSONObject(0);
		assertEquals(tmpParam.length(), 4);
		assertEquals(tmpParam.getString("name"), "ParamA");
		assertEquals(tmpParam.getString("type"), ParameterType.BOOLEAN.toString());
		assertEquals(tmpParam.getString("description"), "bool");
		assertEquals(tmpParam.getBoolean("defaultValue"), true);

		tmpParam = tmpArray.getJSONObject(1);
		assertEquals(tmpParam.length(), 4);
		assertEquals(tmpParam.getString("name"), "ParamB");
		assertEquals(tmpParam.getString("type"), ParameterType.STRING.toString());
		assertEquals(tmpParam.getString("description"), "string");
		assertEquals(tmpParam.getString("defaultValue"), "str");

		tmpParam = tmpArray.getJSONObject(2);
		assertEquals(tmpParam.length(), 4);
		assertEquals(tmpParam.getString("name"), "ParamC");
		assertEquals(tmpParam.getString("type"), ParameterType.STRING.toString());
		assertEquals(tmpParam.getString("description"), "text");
		assertEquals(tmpParam.getString("defaultValue"), "txt");

		tmpParam = tmpArray.getJSONObject(3);
		assertEquals(tmpParam.length(), 4);
		assertEquals(tmpParam.getString("name"), "ParamD");
		assertEquals(tmpParam.getString("type"), ParameterType.STRING.toString());
		assertEquals(tmpParam.getString("description"), "choice");
		assertEquals(tmpParam.getString("defaultValue"), "one");

		tmpParam = tmpArray.getJSONObject(4);
		assertEquals(tmpParam.length(), 4);
		assertEquals(tmpParam.getString("name"), "ParamE");
		assertEquals(tmpParam.getString("type"), ParameterType.UNAVAILABLE.toString());
		assertEquals(tmpParam.getString("description"), "file param");
		assertTrue(tmpParam.isNull("defaultValue"));

		assertTrue(body.has("phasesInternal"));
		tmpArray = body.getJSONArray("phasesInternal");
		assertEquals(tmpArray.length(), 0);

		assertTrue(body.has("phasesPostBuild"));
		tmpArray = body.getJSONArray("phasesPostBuild");
		assertEquals(tmpArray.length(), 0);
	}

	//  Structure test - multi-job, with params, with children
	//
	@Test
	public void testStructureMultiJobWithParamsWithChildren() throws IOException, SAXException {
		MultiJobProject p = rule.getInstance().createProject(MultiJobProject.class, projectName);
		FreeStyleProject p1 = rule.createFreeStyleProject("jobA");
		MatrixProject p2 = rule.createMatrixProject("jobB");
		MultiJobProject p3 = rule.getInstance().createProject(MultiJobProject.class, "jobC");
		MatrixProject p4 = rule.createMatrixProject("jobD");
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
		p.getBuildersList().add(new MultiJobBuilder(
				"Build",
				Arrays.asList(
						new PhaseJobsConfig("jobA", "", false, null, PhaseJobsConfig.KillPhaseOnJobResultCondition.NEVER, false, false, "", 0, false, false, "", false),
						new PhaseJobsConfig("jobB", "", false, null, PhaseJobsConfig.KillPhaseOnJobResultCondition.NEVER, false, false, "", 0, false, false, "", false)
				),
				MultiJobBuilder.ContinuationCondition.SUCCESSFUL
		));
		p.getBuildersList().add(new Shell(""));
		p.getBuildersList().add(new MultiJobBuilder(
				"Test",
				Arrays.asList(
						new PhaseJobsConfig("jobC", "", false, null, PhaseJobsConfig.KillPhaseOnJobResultCondition.NEVER, false, false, "", 0, false, false, "", false),
						new PhaseJobsConfig("jobD", "", false, null, PhaseJobsConfig.KillPhaseOnJobResultCondition.NEVER, false, false, "", 0, false, false, "", false)
				),
				MultiJobBuilder.ContinuationCondition.SUCCESSFUL
		));
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

		page = client.goTo("job/" + projectName + "/octane/structure", "application/json");
		body = new JSONObject(page.getWebResponse().getContentAsString());
		assertEquals(body.length(), 4);
		assertTrue(body.has("name"));
		assertEquals(body.getString("name"), projectName);

		assertTrue(body.has("parameters"));
		tmpArray = body.getJSONArray("parameters");
		assertEquals(tmpArray.length(), 2);

		tmpParam = tmpArray.getJSONObject(0);
		assertEquals(tmpParam.length(), 4);
		assertEquals(tmpParam.getString("name"), "ParamA");
		assertEquals(tmpParam.getString("type"), ParameterType.BOOLEAN.toString());
		assertEquals(tmpParam.getString("description"), "bool");
		assertEquals(tmpParam.getBoolean("defaultValue"), true);

		tmpParam = tmpArray.getJSONObject(1);
		assertEquals(tmpParam.length(), 4);
		assertEquals(tmpParam.getString("name"), "ParamB");
		assertEquals(tmpParam.getString("type"), ParameterType.STRING.toString());
		assertEquals(tmpParam.getString("description"), "string");
		assertEquals(tmpParam.getString("defaultValue"), "str");

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
		assertEquals(tmpPhase.getString("name"), "Build");
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

		tmpPhase = tmpArray.getJSONObject(3);
		assertEquals(tmpPhase.length(), 3);
		assertEquals(tmpPhase.getString("name"), "Test");
		assertEquals(tmpPhase.getBoolean("blocking"), true);
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