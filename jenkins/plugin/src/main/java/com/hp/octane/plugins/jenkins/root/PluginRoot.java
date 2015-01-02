package com.hp.octane.plugins.jenkins.root;

import com.hp.octane.plugins.jenkins.notifications.EventDispatcher;
import hudson.Extension;
import hudson.model.Hudson;
import hudson.model.RootAction;
import org.json.JSONObject;
import org.json.JSONArray;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;
import org.kohsuke.stapler.export.Flavor;

import javax.servlet.ServletException;
import java.io.IOException;

/**
 * Created with IntelliJ IDEA.
 * User: gullery
 * Date: 8/10/14
 * Time: 12:47 PM
 * To change this template use File | Settings | File Templates.
 */
@Extension
public class PluginRoot implements RootAction {

	public String getIconFileName() {
		return null;
	}

	public String getDisplayName() {
		return null;
	}

	public String getUrlName() {
		return "hpDevopsApi";
	}

	public void doAbout(StaplerRequest req, StaplerResponse res) throws IOException, ServletException, InterruptedException {
		res.serveExposedBean(req, new About(), Flavor.JSON);
	}

	public void doDiscover(StaplerRequest req, StaplerResponse res) throws IOException {
		JSONObject resJson = new JSONObject();
		JSONArray tmpArray = new JSONArray();
		resJson.put("type", "jenkins");
		resJson.put("version", Hudson.getVersion().toString());
		for (String name : Hudson.getInstance().getTopLevelItemNames()) tmpArray.put(name);
		resJson.put("projects", tmpArray);
		res.getOutputStream().println(resJson.toString());
		res.flushBuffer();
	}

	public void doClientConfig(StaplerRequest req, StaplerResponse res) throws IOException, ServletException, InterruptedException {
		String inputBody = "";
		JSONObject inputJSON;
		byte[] buffer = new byte[1024];
		int length;
		while (-1 != (length = req.getInputStream().read(buffer))) {
			inputBody += new String(buffer, 0, length);
		}
		inputJSON = new JSONObject(inputBody);
		System.out.println("Accepted client config request for " + inputJSON.getString("clientUri"));
		EventDispatcher.updateClient(inputJSON.getString("clientUri"));
		res.flushBuffer();
	}
}
