package com.hp.octane.plugins.jenkins.actions;

import com.hp.octane.plugins.jenkins.OctanePlugin;
import com.hp.octane.plugins.jenkins.model.api.ParameterConfig;
import com.hp.octane.plugins.jenkins.model.processors.parameters.ParameterProcessors;
import com.hp.octane.plugins.jenkins.notifications.EventDispatcher;
import hudson.Extension;
import hudson.model.AbstractProject;
import hudson.model.RootAction;
import jenkins.model.Jenkins;
import net.sf.json.JSONObject;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;
import org.kohsuke.stapler.export.Exported;
import org.kohsuke.stapler.export.ExportedBean;
import org.kohsuke.stapler.export.Flavor;

import javax.servlet.ServletException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: gullery
 * Date: 8/10/14
 * Time: 12:47 PM
 * To change this template use File | Settings | File Templates.
 */

@Extension
public class PluginActions implements RootAction {

	@ExportedBean
	public static final class ServerInfo {
		private String instanceId;
		private Long instanceIdFrom;
		private String url;
		private final String type = "jenkins";

		public ServerInfo() {
			this.instanceId = Jenkins.getInstance().getPlugin(OctanePlugin.class).getIdentity();
			this.instanceIdFrom = Jenkins.getInstance().getPlugin(OctanePlugin.class).getIdentityFrom();
			String serverUrl = Jenkins.getInstance().getRootUrl();
			if (serverUrl != null && serverUrl.endsWith("/"))
				serverUrl = serverUrl.substring(0, serverUrl.length() - 1);
			this.url = serverUrl;
		}

		@Exported(inline = true)
		public String getInstanceId() {
			return instanceId;
		}

		@Exported(inline = true)
		public Long getInstanceIdFrom() {
			return instanceIdFrom;
		}

		@Exported(inline = true)
		public String getUrl() {
			return url;
		}

		@Exported(inline = true)
		public String getType() {
			return type;
		}
	}

	@ExportedBean
	public static final class PluginInfo {
		private final String version = "1.0.0";

		@Exported(inline = true)
		public String getVersion() {
			return version;
		}
	}

	//  TODO: probably add status collecting logic from all relevant services
	@ExportedBean
	public static final class PluginStatus {
		@Exported(inline = true)
		public ServerInfo getServer() {
			return new ServerInfo();
		}

		@Exported(inline = true)
		public PluginInfo getPlugin() {
			return new PluginInfo();
		}

		@Exported(inline = true)
		public List<EventDispatcher.Client> getEventsClients() {
			return EventDispatcher.getStatus();
		}
	}

	@ExportedBean
	public static final class ProjectConfig {
		private String name;
		private ParameterConfig[] parameters;

		public void setName(String value) {
			name = value;
		}

		@Exported(inline = true)
		public String getName() {
			return name;
		}

		public void setParameters(ParameterConfig[] parameters) {
			this.parameters = parameters;
		}

		@Exported(inline = true)
		public ParameterConfig[] getParameters() {
			return parameters;
		}
	}

	@ExportedBean
	public static final class ProjectsList {
		@Exported(inline = true)
		public ProjectConfig[] jobs;

		public ProjectsList(boolean areParametersNeeded) {
			ProjectConfig tmpConfig;
			AbstractProject tmpProject;
			List<ProjectConfig> list = new ArrayList<ProjectConfig>();
			List<String> itemNames = (List<String>) Jenkins.getInstance().getTopLevelItemNames();
			for (String name : itemNames) {
				tmpProject = (AbstractProject) Jenkins.getInstance().getItem(name);
				tmpConfig = new ProjectConfig();
				tmpConfig.setName(name);
				if (areParametersNeeded) {
					tmpConfig.setParameters(ParameterProcessors.getConfigs(tmpProject));
				}
				list.add(tmpConfig);
			}
			jobs = list.toArray(new ProjectConfig[list.size()]);
		}
	}

	public String getIconFileName() {
		return null;
	}

	public String getDisplayName() {
		return null;
	}

	public String getUrlName() {
		return "octane";
	}

	public void doStatus(StaplerRequest req, StaplerResponse res) throws IOException, ServletException {
		res.serveExposedBean(req, new PluginStatus(), Flavor.JSON);
	}

	public void doJobs(StaplerRequest req, StaplerResponse res) throws IOException, ServletException {
		boolean areParametersNeeded = true;
		if (req.getParameter("parameters") != null && req.getParameter("parameters").toLowerCase().equals("false")) {
			areParametersNeeded = false;
		}
		res.serveExposedBean(req, new ProjectsList(areParametersNeeded), Flavor.JSON);
	}

	public void doConfig(StaplerRequest req, StaplerResponse res) throws IOException, ServletException {
		String body = "";
		JSONObject inputJSON;
		byte[] buffer = new byte[1024];
		int length;
		while ((length = req.getInputStream().read(buffer)) > 0) body += new String(buffer, 0, length);

		if (body.length() > 0) {
			inputJSON = JSONObject.fromObject(body);
			String url;
			String domain;
			String project;
			String username;
			String password;
			if (inputJSON.containsKey("type") && inputJSON.getString("type").equals("events-client")) {
				url = inputJSON.getString("url");
				domain = inputJSON.getString("domain");
				project = inputJSON.getString("project");
				username = inputJSON.getString("username");
				password = inputJSON.getString("password");
				System.out.println("Accepted events-client config request for '" + url + "', '" + domain + "', '" + project + "'");
				EventDispatcher.updateClient(url, domain, project, username, password);
			}
		}
		EventDispatcher.wakeUpClients();
	}
}
