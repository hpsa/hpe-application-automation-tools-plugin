/*
 *     Copyright 2017 Hewlett-Packard Development Company, L.P.
 *     Licensed under the Apache License, Version 2.0 (the "License");
 *     you may not use this file except in compliance with the License.
 *     You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS,
 *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *     See the License for the specific language governing permissions and
 *     limitations under the License.
 *
 */

package com.hpe.application.automation.tools.octane.configuration;

import com.hpe.application.automation.tools.model.OctaneServerSettingsModel;
import hudson.util.FormValidation;
import hudson.util.Secret;
import jenkins.model.Jenkins;
import net.sf.json.JSONObject;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;
import org.kohsuke.stapler.export.Exported;
import org.kohsuke.stapler.export.ExportedBean;
import org.kohsuke.stapler.export.Flavor;
import org.kohsuke.stapler.interceptor.RequirePOST;

import javax.servlet.ServletException;
import java.io.IOException;

public class ConfigApi {
	private static final Logger logger = LogManager.getLogger(ConfigApi.class);

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
				res.sendError(400, "Either (uiLocation) or (location and shared space) must be specified");
				return;
			}
			uiLocation = location.replaceAll("/$", "") + "/ui?p=" + sharedSpace;
		} else {
			uiLocation = configuration.getString("uiLocation");
		}
		try {
			// validate location format
			ConfigurationParser.parseUiLocation(uiLocation);
		} catch (FormValidation ex) {
			res.sendError(400, ex.getMessage());
			return;
		}

		String impersonatedUser = configuration.containsKey("impersonatedUser") ? configuration.getString("impersonatedUser") : "";

		String username;
		Secret password;
		if (!configuration.containsKey("username")) {
			// when username is not provided, use existing credentials (password can be overridden later)
			ServerConfiguration serverConfiguration = ConfigurationService.getServerConfiguration();
			username = serverConfiguration.username;
			password = serverConfiguration.password;
		} else {
			// when username is provided, clear password unless provided later
			username = configuration.getString("username");
			password = Secret.fromString("");
		}
		if (configuration.containsKey("password")) {
			password = Secret.fromString(configuration.getString("password"));
		}
		OctaneServerSettingsModel model = new OctaneServerSettingsModel(uiLocation, username, password, impersonatedUser);
		ConfigurationService.configurePlugin(model);

		String serverIdentity = (String) configuration.get("serverIdentity");
		if (!StringUtils.isEmpty(serverIdentity)) {
			ConfigurationService.getModel().setIdentity(serverIdentity);
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
				serverConfiguration.impersonatedUser,
				ConfigurationService.getModel().getIdentity());
	}

	@ExportedBean
	public static final class Configuration {

		private String location;
		private String sharedSpace;
		private String username;
		private String serverIdentity;
		private String impersonatedUser;


		public Configuration(String location, String sharedSpace, String username, String impersonatedUser, String serverIdentity) {
			this.location = location;
			this.sharedSpace = sharedSpace;
			this.username = username;
			this.impersonatedUser = impersonatedUser;
			this.serverIdentity = serverIdentity;
		}

		@Exported(inline = true)
		public String getLocation() {
			return location;
		}

		@Exported(inline = true)
		public String getImpersonatedUser() {
			return impersonatedUser;
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
