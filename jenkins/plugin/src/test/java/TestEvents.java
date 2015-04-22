import com.gargoylesoftware.htmlunit.HttpMethod;
import com.gargoylesoftware.htmlunit.WebRequestSettings;
import com.gargoylesoftware.htmlunit.WebResponse;
import com.hp.octane.plugins.jenkins.actions.PluginActions;
import com.hp.octane.plugins.jenkins.model.events.CIEventBase;
import com.hp.octane.plugins.jenkins.model.events.CIEventType;
import hudson.model.FreeStyleProject;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.AbstractHandler;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.*;
import org.jvnet.hudson.test.JenkinsRule;
import org.jvnet.hudson.test.recipes.WithTimeout;
import org.kohsuke.stapler.export.Exported;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

import static org.junit.Assert.*;

/**
 * Created with IntelliJ IDEA.
 * User: gullery
 * Date: 13/01/15
 * Time: 22:05
 * To change this template use File | Settings | File Templates.
 */

public class TestEvents {
	final private String projectName = "root-job";
	final private int DEFAULT_TESTING_SERVER_PORT = 9999;

	private Server server;
	private EventsHandler eventsHandler;
	private int testingServerPort = DEFAULT_TESTING_SERVER_PORT;

	@Rule
	public final JenkinsRule rule = new JenkinsRule();

	private static final class EventsHandler extends AbstractHandler {
		private final ArrayList<JSONObject> eventLists = new ArrayList<JSONObject>();

		@Override
		public void handle(String s, Request baseRequest, HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
			String body = "";
			byte[] buffer;
			int len;
			if (request.getPathInfo().equals("/qcbin/api/domains/DOMAIN/projects/PROJECT/cia/events")) {
				buffer = new byte[1024];
				while ((len = request.getInputStream().read(buffer, 0, 1024)) > 0) {
					body += new String(buffer, 0, len);
				}
				eventLists.add(new JSONObject(body));
			}
			response.setStatus(HttpServletResponse.SC_OK);
			baseRequest.setHandled(true);
		}

		public ArrayList<JSONObject> getResults() {
			return eventLists;
		}

		public void clearResults() {
			eventLists.clear();
		}
	}

	public TestEvents() {
		String p = System.getProperty("testingServerPort");
		try {
			if (p != null) testingServerPort = Integer.parseInt(p);
		} catch (NumberFormatException nfe) {
			System.out.println("Bad port number format, default port will be used: " + testingServerPort);
		}
	}

	private void raiseServer() throws Exception {
		eventsHandler = new EventsHandler();
		server = new Server(testingServerPort);
		server.setHandler(eventsHandler);
		server.start();
	}

	private void configEventsClient() throws Exception {
		JenkinsRule.WebClient client = rule.createWebClient();
		WebRequestSettings req = new WebRequestSettings(client.createCrumbedUrl("octane/config"), HttpMethod.POST);
		JSONObject json = new JSONObject();
		json.put("type", "events-client");
		json.put("url", "http://localhost:" + testingServerPort + "/qcbin");
		json.put("domain", "DOMAIN");
		json.put("project", "PROJECT");
		json.put("username", "");
		json.put("password", "");
		req.setRequestBody(json.toString());
		WebResponse res = client.loadWebResponse(req);
		System.out.println("Configuration submitted with result: " + res.getStatusMessage() + "; testing server will run on port " + testingServerPort);
	}

	private void killServer() throws Exception {
		server.stop();
		server.destroy();
	}

	@Before
	public void beforeEach() throws Exception {
		raiseServer();
		configEventsClient();
	}

	@After
	public void afterEach() throws Exception {
		killServer();
	}

	@Test
	@WithTimeout(360)
	public void testEventsA() throws Exception {
		FreeStyleProject p = rule.createFreeStyleProject(projectName);
		JenkinsRule.WebClient client = rule.createWebClient();
		assertEquals(p.getBuilds().toArray().length, 0);
		Utils.buildProject(client, p);
		while (p.getLastBuild() == null || p.getLastBuild().isBuilding()) {
			Thread.sleep(1000);
		}
		assertEquals(p.getBuilds().toArray().length, 1);
		Thread.sleep(5000);

		ArrayList<CIEventType> eventsOrder = new ArrayList<CIEventType>(Arrays.asList(CIEventType.QUEUED, CIEventType.STARTED, CIEventType.FINISHED));
		ArrayList<JSONObject> eventLists = eventsHandler.getResults();
		JSONObject tmp;
		JSONArray events;
		for (JSONObject l : eventLists) {
			assertEquals(l.length(), 2);

			assertFalse(l.isNull("server"));
			tmp = l.getJSONObject("server");
			assertEquals(tmp.getString("url"), new PluginActions.ServerInfo().getUrl());
			assertEquals(tmp.getString("type"), new PluginActions.ServerInfo().getType());
			assertEquals(tmp.getString("instanceId"), new PluginActions.ServerInfo().getInstanceId());

			assertFalse(l.isNull("events"));
			events = l.getJSONArray("events");
			for (int i = 0; i < events.length(); i++) {
				tmp = events.getJSONObject(i);
				if (tmp.getString("project").equals("root-job")) {
					assertEquals(CIEventType.getByValue(tmp.getString("eventType")), eventsOrder.get(0));
					eventsOrder.remove(0);
				}
			}
		}
		//assertEquals(0, eventsOrder.size());
	}
}
