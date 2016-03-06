package com.hp.octane.plugins.jenkins.actions.plugin;

/**
 * Created with IntelliJ IDEA.
 * User: gullery
 * Date: 07/01/15
 * Time: 22:09
 * To change this template use File | Settings | File Templates.
 */

public class PluginActionsTest {
//	final private String projectName = "root-job";
//
//	@Rule
//	final public JenkinsRule rule = new JenkinsRule();
//
//	@Test
//	public void testPluginActionsMethods() {
//		PluginActions pluginActions = new PluginActions();
//		assertEquals(pluginActions.getIconFileName(), null);
//		assertEquals(pluginActions.getDisplayName(), null);
//		assertEquals(pluginActions.getUrlName(), "octane");
//	}
//
//	@Test
//	public void testPluginActions_REST_Status() throws IOException, SAXException {
//		JenkinsRule.WebClient client = rule.createWebClient();
//		Page page = client.goTo("octane/status", "application/json");
//		JSONObject body = new JSONObject(page.getWebResponse().getContentAsString());
//		JSONObject tmp;
//
//		assertEquals(2, body.length());
//
//		assertTrue(body.has("server"));
//		tmp = body.getJSONObject("server");
//		assertEquals(CIServerTypes.JENKINS.value(), tmp.getString("type"));
//		assertEquals(Jenkins.VERSION, tmp.getString("version"));
//		assertEquals(rule.getInstance().getRootUrl(), tmp.getString("url") + "/");
//		assertEquals(Jenkins.getInstance().getPlugin(OctanePlugin.class).getIdentity(), tmp.get("instanceId"));
//		assertEquals(Jenkins.getInstance().getPlugin(OctanePlugin.class).getIdentityFrom(), (Long) tmp.getLong("instanceIdFrom"));
//		assertFalse(tmp.isNull("sendingTime"));
//
//		assertTrue(body.has("plugin"));
//		tmp = body.getJSONObject("plugin");
//		assertEquals(Jenkins.getInstance().getPlugin(OctanePlugin.class).getWrapper().getVersion(), tmp.getString("version"));
//	}
//
//	@Test
//	public void testPluginActions_REST_Jobs_NoParams() throws IOException, SAXException {
//		JenkinsRule.WebClient client = rule.createWebClient();
//		Page page;
//
//		page = client.goTo("octane/jobs", "application/json");
//		ProjectsList response = SerializationService.fromJSON(page.getWebResponse().getContentAsString(), ProjectsList.class);
//
//		assertNotNull(response);
//		assertNotNull(response.getJobs());
//		assertEquals(0, response.getJobs().length);
//
//		rule.createFreeStyleProject(projectName);
//		page = client.goTo("octane/jobs", "application/json");
//		response = SerializationService.fromJSON(page.getWebResponse().getContentAsString(), ProjectsList.class);
//
//		assertNotNull(response);
//		assertNotNull(response.getJobs());
//		assertEquals(1, response.getJobs().length);
//		assertEquals(projectName, response.getJobs()[0].getName());
//		assertNotNull(response.getJobs()[0].getParameters());
//		assertEquals(0, response.getJobs()[0].getParameters().length);
//	}
//
//	@Test
//	public void testPluginActions_REST_Jobs_WithParams() throws IOException, SAXException {
//		FreeStyleProject fsp;
//		JenkinsRule.WebClient client = rule.createWebClient();
//		Page page;
//		JSONObject body;
//		JSONArray jobs;
//
//		page = client.goTo("octane/jobs", "application/json");
//		ProjectsList response = SerializationService.fromJSON(page.getWebResponse().getContentAsString(), ProjectsList.class);
//
//		assertNotNull(response);
//		assertNotNull(response.getJobs());
//		assertEquals(0, response.getJobs().length);
//
//		fsp = rule.createFreeStyleProject(projectName);
//		ParametersDefinitionProperty params = new ParametersDefinitionProperty(Arrays.asList(
//				(ParameterDefinition) new BooleanParameterDefinition("ParamA", true, "bool"),
//				(ParameterDefinition) new StringParameterDefinition("ParamB", "str", "string"),
//				(ParameterDefinition) new FileParameterDefinition("ParamC", "file param")
//		));
//		fsp.addProperty(params);
//
//		page = client.goTo("octane/jobs", "application/json");
//		response = SerializationService.fromJSON(page.getWebResponse().getContentAsString(), ProjectsList.class);
//
//		assertNotNull(response);
//		assertNotNull(response.getJobs());
//		assertEquals(1, response.getJobs().length);
//		assertEquals(projectName, response.getJobs()[0].getName());
//		assertNotNull(response.getJobs()[0].getParameters());
//		assertEquals(3, response.getJobs()[0].getParameters().length);
//
//		//  Test ParamA
//		assertNotNull(response.getJobs()[0].getParameters()[0]);
//		assertEquals("ParamA", response.getJobs()[0].getParameters()[0].getName());
//		assertEquals(ParameterType.BOOLEAN, response.getJobs()[0].getParameters()[0].getType());
//		assertEquals("bool", response.getJobs()[0].getParameters()[0].getDescription());
//		assertEquals(true, response.getJobs()[0].getParameters()[0].getDefaultValue());
//
//		//  Test ParamB
//		assertNotNull(response.getJobs()[0].getParameters()[1]);
//		assertEquals("ParamB", response.getJobs()[0].getParameters()[1].getName());
//		assertEquals(ParameterType.STRING, response.getJobs()[0].getParameters()[1].getType());
//		assertEquals("string", response.getJobs()[0].getParameters()[1].getDescription());
//		assertEquals("str", response.getJobs()[0].getParameters()[1].getDefaultValue());
//
//		//  Test ParamC
//		assertNotNull(response.getJobs()[0].getParameters()[2]);
//		assertEquals("ParamC", response.getJobs()[0].getParameters()[2].getName());
//		assertEquals(ParameterType.FILE, response.getJobs()[0].getParameters()[2].getType());
//		assertEquals("file param", response.getJobs()[0].getParameters()[2].getDescription());
//		assertEquals("", response.getJobs()[0].getParameters()[2].getDefaultValue());
//	}
}
