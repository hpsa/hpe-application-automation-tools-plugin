package com.hp.octane.plugins.jenkins.actions;

import com.hp.nga.integrations.dto.rest.NGAResult;
import com.hp.nga.integrations.dto.rest.NGATask;
import com.hp.nga.integrations.services.bridge.NGATaskProcessor;
import com.hp.octane.plugins.jenkins.OctanePlugin;
import hudson.Extension;
import hudson.model.RootAction;
import jenkins.model.Jenkins;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;
import org.kohsuke.stapler.export.Exported;
import org.kohsuke.stapler.export.ExportedBean;

import javax.servlet.ServletException;
import java.io.IOException;
import java.util.UUID;
import java.util.logging.Logger;

/**
 * Created with IntelliJ IDEA.
 * User: gullery
 * Date: 8/10/14
 * Time: 12:47 PM
 * To change this template use File | Settings | File Templates.
 */

@Extension
public class PluginActions implements RootAction {
	private static final Logger logger = Logger.getLogger(PluginActions.class.getName());

	//  [YG] probably move the instance ID related things to Plugin Info, since it's not belongs to the Jenkins core
	@ExportedBean
	public static final class ServerInfo {
		private static final String type = "jenkins";
		private static final String version = Jenkins.VERSION;
		private String url;
		private String instanceId = Jenkins.getInstance().getPlugin(OctanePlugin.class).getIdentity();
		private Long instanceIdFrom = Jenkins.getInstance().getPlugin(OctanePlugin.class).getIdentityFrom();
		private Long sendingTime;

		public ServerInfo() {
			String serverUrl = Jenkins.getInstance().getRootUrl();
			if (serverUrl != null && serverUrl.endsWith("/"))
				serverUrl = serverUrl.substring(0, serverUrl.length() - 1);
			this.url = serverUrl;
			this.sendingTime = System.currentTimeMillis();
		}

		@Exported(inline = true)
		public String getType() {
			return type;
		}

		@Exported(inline = true)
		public String getVersion() {
			return version;
		}

		@Exported(inline = true)
		public String getUrl() {
			return url;
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
		public Long getSendingTime() {
			return sendingTime;
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

	public void doDynamic(StaplerRequest req, StaplerResponse res) throws IOException, ServletException {
		NGATask ngaTask = new NGATask();
		ngaTask.setId(UUID.randomUUID().toString());
		ngaTask.setMethod(req.getMethod());
		ngaTask.setUrl(req.getRequestURIWithQueryString());
		ngaTask.setBody("");
		NGATaskProcessor taskProcessor = new NGATaskProcessor(ngaTask);
		NGAResult result = taskProcessor.execute();

		res.setStatus(result.getStatus());
		if (result.getBody() != null) {
			res.getWriter().write(result.getBody());
		}
	}
}
