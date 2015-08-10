import com.gargoylesoftware.htmlunit.Page;
import com.hp.octane.plugins.jenkins.model.parameters.ParameterType;
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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Created with IntelliJ IDEA.
 * User: gullery
 * Date: 13/01/15
 * Time: 11:41
 * To change this template use File | Settings | File Templates.
 */

public class ProjectActionsMatrixTest {
	final private String projectName = "root-job";

	@Rule
	final public JenkinsRule rule = new JenkinsRule();

	//  Structure test: matrix, no params, no children
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

	//  Structure test: matrix, with params, no children
	//
	@Test
	public void testStructureMatrixWithParamsNoChildren() throws IOException, SAXException {
		MatrixProject p = rule.createMatrixProject(projectName);
		ParametersDefinitionProperty params = new ParametersDefinitionProperty(Arrays.asList(
				(ParameterDefinition) new BooleanParameterDefinition("ParamA", true, "bool"),
				(ParameterDefinition) new StringParameterDefinition("ParamB", "str", "string"),
				(ParameterDefinition) new TextParameterDefinition("ParamC", "txt", "text"),
				(ParameterDefinition) new ChoiceParameterDefinition("ParamD", new String[]{"1", "2", "3"}, "choice"),
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
		assertEquals(tmpParam.getString("defaultValue"), "1");
		assertNotNull(tmpParam.get("choices"));
		assertEquals(tmpParam.getJSONArray("choices").length(), 3);
		assertEquals(tmpParam.getJSONArray("choices").get(0), "1");
		assertEquals(tmpParam.getJSONArray("choices").get(1), "2");
		assertEquals(tmpParam.getJSONArray("choices").get(2), "3");

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

	//  Structure test: matrix, with params, with children
	//
	@Test
	public void testStructureMatrixWithParamsWithChildren() throws IOException, SAXException {
		MatrixProject p = rule.createMatrixProject(projectName);
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
		p.getPublishersList().add(new Fingerprinter(""));
		p.getPublishersList().add(new BuildTrigger("jobA, jobB, JobE", Result.SUCCESS));
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
