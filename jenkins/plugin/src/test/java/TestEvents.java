import com.hp.octane.plugins.jenkins.model.events.CIEventBase;
import hudson.model.FreeStyleProject;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.AbstractHandler;
import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertEquals;

/**
 * Created with IntelliJ IDEA.
 * User: gullery
 * Date: 13/01/15
 * Time: 22:05
 * To change this template use File | Settings | File Templates.
 */

public class TestEvents {
	final private String projectName = "root-job";
	private Server server;

	@Rule
	final public JenkinsRule rule = new JenkinsRule();

	private final class EventsHandler extends AbstractHandler {
		private final HashMap<String, ArrayList<CIEventBase>> events = new HashMap<String, ArrayList<CIEventBase>>();

		@Override
		public void handle(String s, Request baseRequest, HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
			response.setStatus(HttpServletResponse.SC_OK);
			baseRequest.setHandled(true);
		}
	}

	private void raiseServer() throws Exception {
		server = new Server(9999);       //  TODO: make it configurable
		server.setHandler(new EventsHandler());
		server.start();
	}

	private void killServer() throws Exception {
		server.stop();
		server.destroy();
	}

//	@Test
	public void testEvents() throws Exception {
		raiseServer();

		FreeStyleProject p = rule.createFreeStyleProject(projectName);
		JenkinsRule.WebClient client = rule.createWebClient();
		assertEquals(p.getBuilds().toArray().length, 0);
		Utils.buildProject(client, p);
		while (p.getLastBuild() == null || p.getLastBuild().isBuilding()) {
		}
		assertEquals(p.getBuilds().toArray().length, 1);
		Thread.sleep(100);      //  give a chance to the finished event to be processed

		//  process the accumulated events here

		killServer();
	}
}
