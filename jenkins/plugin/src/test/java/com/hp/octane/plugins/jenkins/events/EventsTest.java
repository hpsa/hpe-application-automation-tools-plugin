package com.hp.octane.plugins.jenkins.events;

import com.gargoylesoftware.htmlunit.HttpMethod;
import com.gargoylesoftware.htmlunit.WebRequest;
import com.gargoylesoftware.htmlunit.WebResponse;
import com.hp.octane.integrations.dto.events.CIEventType;
import com.hp.octane.plugins.jenkins.ExtensionUtil;
import com.hp.octane.plugins.jenkins.configuration.ServerConfiguration;
import hudson.model.FreeStyleProject;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.AbstractHandler;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.*;
import org.jvnet.hudson.test.JenkinsRule;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;

import static org.junit.Assert.*;

/**
 * Created with IntelliJ IDEA.
 * User: gullery
 * Date: 13/01/15
 * Time: 22:05
 * To change this template use File | Settings | File Templates.
 */

public class EventsTest {
	private static final Logger logger = Logger.getLogger(EventsTest.class.getName());

	static final private String projectName = "root-job-events-case";
	static final private int DEFAULT_TESTING_SERVER_PORT = 9999;
	static final private String sharedSpaceId = "1007";
	static final private String username = "some";
	static final private String password = "pass";

	static private Server server;
	static private int testingServerPort = DEFAULT_TESTING_SERVER_PORT;
	static private EventsHandler eventsHandler;

	@ClassRule
	public static final JenkinsRule rule = new JenkinsRule();
	private final JenkinsRule.WebClient client = rule.createWebClient();

	private static final class EventsHandler extends AbstractHandler {
		private final List<JSONObject> eventsLists = new ArrayList<>();

		@Override
		public void handle(String s, Request baseRequest, HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
			logger.info("EVENTS TEST: server mock requested: " + baseRequest.getMethod() + " " + baseRequest.getPathInfo());

			String body = "";
			byte[] buffer;
			int len;
			if (request.getPathInfo().equals("/authentication/sign_in")) {
				response.setStatus(HttpServletResponse.SC_OK);
			} else if (request.getPathInfo().equals("/internal-api/shared_spaces/" + sharedSpaceId + "/analytics/ci/events")) {
				buffer = new byte[1024];
				while ((len = request.getInputStream().read(buffer, 0, 1024)) > 0) {
					body += new String(buffer, 0, len);
				}
				try {
					eventsLists.add(new JSONObject(body));
				} catch (JSONException e) {
					logger.warning("EVENTS TEST: response wasn't JSON compatible");
				}
				logger.info("EVENTS TEST: server mock events list length " + eventsLists.size());
				response.setStatus(HttpServletResponse.SC_OK);
			}
			baseRequest.setHandled(true);
		}

		List<JSONObject> getResults() {
			return eventsLists;
		}

		public void clearResults() {
			eventsLists.clear();
		}
	}

	public EventsTest() {
		String p = System.getProperty("testingServerPort");
		try {
			if (p != null) testingServerPort = Integer.parseInt(p);
		} catch (NumberFormatException nfe) {
			logger.info("EVENTS TEST: bad port number format, default port will be used: " + testingServerPort);
		}
	}

	private void configEventsClient() throws Exception {
		EventsService eventsService = ExtensionUtil.getInstance(rule, EventsService.class);
		eventsService.updateClient(new ServerConfiguration(
				"http://localhost:" + testingServerPort,
				sharedSpaceId,
				username,
				password,
				""
		));

		//WebRequestSettings req = new WebRequestSettings(client.createCrumbedUrl("nga/status"), HttpMethod.GET);
		// the above 1 line changed to this 1 line
		WebRequest req = new WebRequest(client.createCrumbedUrl("nga/api/v1/status"), HttpMethod.GET);

		WebResponse res = client.loadWebResponse(req);
		JSONObject resJSON = new JSONObject(res.getContentAsString());
		//assertEquals("", resJSON.toString());
		//assertEquals(1, resJSON.getJSONArray("eventsClients").length());
		//assertEquals("http://localhost:" + testingServerPort, resJSON.getJSONArray("eventsClients").getJSONObject(0).getString("location"));
		//assertEquals(sharedSpaceId, resJSON.getJSONArray("eventsClients").getJSONObject(0).getString("sharedSpace"));
		logger.info("EVENTS TEST: plugin status of '" + client.getContextPath() + "': " + res.getContentAsString());
	}

	@BeforeClass
	static public void beforeClass() throws Exception {
		eventsHandler = new EventsHandler();
		server = new Server(testingServerPort);
		server.setHandler(eventsHandler);
		server.start();
	}

	@AfterClass
	static public void afterClass() throws Exception {
		server.stop();
		server.destroy();
	}

	@Test
	@Ignore
	public void testEventsA() throws Exception {
		FreeStyleProject p = rule.createFreeStyleProject(projectName);

		configEventsClient();

		EventsService eventsService = ExtensionUtil.getInstance(rule, EventsService.class);
		assertEquals(1, eventsService.getStatus().size());
		assertEquals("http://localhost:" + testingServerPort, eventsService.getStatus().get(0).getLocation());
		assertEquals(sharedSpaceId, eventsService.getStatus().get(0).getSharedSpace());
		assertEquals(1, rule.jenkins.getTopLevelItemNames().size());
		assertTrue(rule.jenkins.getTopLevelItemNames().contains(projectName));

		assertEquals(0, p.getBuilds().toArray().length);
		p.scheduleBuild(0, null);
		while (p.getLastBuild() == null || p.getLastBuild().isBuilding()) {
			Thread.sleep(1000);
		}
		assertEquals(1, p.getBuilds().toArray().length);
		Thread.sleep(5000);

		List<CIEventType> eventsOrder = new ArrayList<CIEventType>(Arrays.asList(CIEventType.STARTED, CIEventType.FINISHED));
		List<JSONObject> eventsLists = eventsHandler.getResults();
		JSONObject tmp;
		JSONArray events;
		logger.info(eventsLists.toString());
		System.out.print(eventsLists.toString());
		logger.info("EVENTS TEST: server mock received " + eventsLists.size() + " list/s of events");
		for (JSONObject l : eventsLists) {
			assertEquals(2, l.length());

			assertFalse(l.isNull("server"));
			tmp = l.getJSONObject("server");
			//assertEquals("url", tmp.getString("url"));
			//assertEquals("jenkins", tmp.getString("type"));
			//assertEquals("instance", tmp.getString("instanceId"));

			assertFalse(l.isNull("events"));
			events = l.getJSONArray("events");
			for (int i = 0; i < events.length(); i++) {
				tmp = events.getJSONObject(i);
				if (tmp.getString("project").equals(projectName)) {
					assertEquals(eventsOrder.get(0), CIEventType.fromValue(tmp.getString("eventType")));
					eventsOrder.remove(0);
				}
			}
		}
		assertEquals(0, eventsOrder.size());
	}
}
