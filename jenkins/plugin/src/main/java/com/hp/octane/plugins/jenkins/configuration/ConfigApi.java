// (C) Copyright 2003-2015 Hewlett-Packard Development Company, L.P.

package com.hp.octane.plugins.jenkins.configuration;

import com.hp.octane.plugins.jenkins.OctanePlugin;
import com.hp.octane.plugins.jenkins.identity.ServerIdentity;
import hudson.util.FormValidation;
import jenkins.model.Jenkins;
import net.sf.json.JSONObject;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;
import org.kohsuke.stapler.export.Exported;
import org.kohsuke.stapler.export.ExportedBean;
import org.kohsuke.stapler.export.Flavor;
import org.kohsuke.stapler.interceptor.RequirePOST;

import javax.servlet.ServletException;
import java.io.IOException;
import java.util.logging.Logger;

public class ConfigApi {
	private static final Logger logger = Logger.getLogger(ConfigApi.class.getName());

	public void doRead(StaplerRequest req, StaplerResponse res) throws ServletException, IOException {
		checkPermission();
		res.serveExposedBean(req, getConfiguration(), Flavor.JSON);
	}

	@RequirePOST
	public void doSave(StaplerRequest req, StaplerResponse res) throws IOException, ServletException {
		checkPermission();

		JSONObject configuration = JSONObject.fromObject(IOUtils.toString(req.getInputStream()));
		String uiLocation;
		if (!configuration.containsKey("uiLocation")) {
			// allow per-partes project specification
			String location = (String) configuration.get("location");
			String sharedSpace = (String) configuration.get("sharedSpace");
			if (StringUtils.isEmpty(location) || StringUtils.isEmpty(sharedSpace)) {
				res.sendError(400, "Either (uiLocation) or (location, sharedSpace and project) must be specified");
				return;
			}
			uiLocation = location.replaceAll("/$", "") + "/ui?p=" + sharedSpace;
		} else {
			uiLocation = configuration.getString("uiLocation");
		}
//		try {
//			// validate location format
//			ConfigurationService.parseUiLocation(uiLocation);
//		} catch (FormValidation ex) {
//			res.sendError(400, ex.getMessage());
//			return;
//		}
		String username, password;
		if (!configuration.containsKey("username")) {
			// when username is not provided, use existing credentials (password can be overridden later)
			ServerConfiguration serverConfiguration = ConfigurationService.getServerConfiguration();
			username = serverConfiguration.username;
			password = serverConfiguration.password;
		} else {
			// when username is provided, clear password unless provided later
			username = configuration.getString("username");
			password = "";
		}
		if (configuration.containsKey("password")) {
			password = configuration.getString("password");
		}
		OctanePlugin octanePlugin = Jenkins.getInstance().getPlugin(OctanePlugin.class);
		octanePlugin.configurePlugin(uiLocation, username, password);
		String serverIdentity = (String) configuration.get("serverIdentity");
		if (!StringUtils.isEmpty(serverIdentity)) {
			octanePlugin.setIdentity(serverIdentity);
		}

		res.serveExposedBean(req, getConfiguration(), Flavor.JSON);
	}

	private void checkPermission() {
		Jenkins.getInstance().getACL().checkPermission(Jenkins.ADMINISTER);
	}

	private Configuration getConfiguration() {
		ServerConfiguration serverConfiguration = ConfigurationService.getServerConfiguration();
		return new Configuration(
				serverConfiguration.location,
				serverConfiguration.sharedSpace,
				serverConfiguration.username,
				ServerIdentity.getIdentity());
	}

	@ExportedBean
	public static final class Configuration {

		private String location;
		private String sharedSpace;
		private String username;
		private String serverIdentity;

		public Configuration(String location, String sharedSpace, String username, String serverIdentity) {
			this.location = location;
			this.sharedSpace = sharedSpace;
			this.username = username;
			this.serverIdentity = serverIdentity;
		}

		@Exported(inline = true)
		public String getLocation() {
			return location;
		}

		@Exported(inline = true)
		public String getSharedSpace() {
			return sharedSpace;
		}

		@Exported(inline = true)
		public String getUsername() {
			return username;
		}

		@Exported(inline = true)
		public String getServerIdentity() {
			return serverIdentity;
		}
	}
}
