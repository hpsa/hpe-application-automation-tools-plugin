/*
 *  Certain versions of software and/or documents (“Material”) accessible here may contain branding from
 *  Hewlett-Packard Company (now HP Inc.) and Hewlett Packard Enterprise Company.  As of September 1, 2017,
 *  the Material is now offered by Micro Focus, a separately owned and operated company.  Any reference to the HP
 *  and Hewlett Packard Enterprise/HPE marks is historical in nature, and the HP and Hewlett Packard Enterprise/HPE
 *  marks are the property of their respective owners.
 * __________________________________________________________________
 * MIT License
 *
 * © Copyright 2012-2018 Micro Focus or one of its affiliates.
 *
 * The only warranties for products and services of Micro Focus and its affiliates
 * and licensors (“Micro Focus”) are set forth in the express warranty statements
 * accompanying such products and services. Nothing herein should be construed as
 * constituting an additional warranty. Micro Focus shall not be liable for technical
 * or editorial errors or omissions contained herein.
 * The information contained herein is subject to change without notice.
 * ___________________________________________________________________
 */

package com.microfocus.application.automation.tools.octane.configuration;

import com.microfocus.application.automation.tools.model.OctaneServerSettingsModel;
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
import java.util.LinkedList;
import java.util.List;

public class ConfigApi {
	private static final Logger logger = LogManager.getLogger(ConfigApi.class);

	public void doRead(StaplerRequest req, StaplerResponse res) throws ServletException, IOException {
		checkPermission();
		res.serveExposedBean(req, getConfigurations(), Flavor.JSON);
	}

	@RequirePOST
	public void doSave(StaplerRequest req, StaplerResponse res) throws IOException, ServletException {
		checkPermission();

		JSONObject newConfiguration = JSONObject.fromObject(IOUtils.toString(req.getInputStream()));
		String uiLocation;
		if (!newConfiguration.containsKey("uiLocation")) {
			// allow per-partes project specification
			String location = (String) newConfiguration.get("location");
			String sharedSpace = (String) newConfiguration.get("sharedSpace");
			if (StringUtils.isEmpty(location) || StringUtils.isEmpty(sharedSpace)) {
				res.sendError(400, "Either (uiLocation) or (location and shared space) must be specified");
				return;
			}
			uiLocation = location.replaceAll("/$", "") + "/ui?p=" + sharedSpace;
		} else {
			uiLocation = newConfiguration.getString("uiLocation");
		}
		try {
			// validate location format
			ConfigurationParser.parseUiLocation(uiLocation);
		} catch (FormValidation ex) {
			res.sendError(400, ex.getMessage());
			return;
		}

		String impersonatedUser = newConfiguration.containsKey("impersonatedUser") ? newConfiguration.getString("impersonatedUser") : "";

		String username = "";
		Secret password = Secret.fromString("");
		if (newConfiguration.containsKey("username")) {
			username = newConfiguration.getString("username");
		}
		if (newConfiguration.containsKey("password")) {
			password = Secret.fromString(newConfiguration.getString("password"));
		}
		OctaneServerSettingsModel model = new OctaneServerSettingsModel(uiLocation, username, password, impersonatedUser, null);
		ConfigurationService.configurePlugin(model);

		res.serveExposedBean(req, getConfiguration(model.getIdentity()), Flavor.JSON);
	}

	private void checkPermission() {
		Jenkins.getInstance().getACL().checkPermission(Jenkins.ADMINISTER);
	}

	private Configuration getConfiguration(String identity) {
		OctaneServerSettingsModel settings = ConfigurationService.getSettings(identity);
		return new Configuration(
				settings.getLocation(),
				settings.getSharedSpace(),
				settings.getUsername(),
				settings.getImpersonatedUser(),
				settings.getIdentity());
	}

	private Configurations getConfigurations() {
		List<Configuration> result = new LinkedList<>();
		ConfigurationService.getAllSettings().forEach(one -> {
			if (one.isValid()) {
				result.add(new Configuration(
						one.getLocation(),
						one.getSharedSpace(),
						one.getUsername(),
						one.getImpersonatedUser(),
						one.getIdentity()));
			}
		});
		return new Configurations(result);
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
		public String getSharedSpace() {
			return sharedSpace;
		}

		@Exported(inline = true)
		public String getUsername() {
			return username;
		}

		@Exported(inline = true)
		public String getImpersonatedUser() {
			return impersonatedUser;
		}

		@Exported(inline = true)
		public String getServerIdentity() {
			return serverIdentity;
		}
	}

	@ExportedBean
	public static final class Configurations {
		private final List<Configuration> configurations;

		public Configurations(List<Configuration> configurations) {
			this.configurations = new LinkedList<>(configurations);
		}

		@Exported(inline = true)
		public Configuration[] getConfigurations() {
			return configurations.toArray(new Configuration[0]);
		}
	}
}
