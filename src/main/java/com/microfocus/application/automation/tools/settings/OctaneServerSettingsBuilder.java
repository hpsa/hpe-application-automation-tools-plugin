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

package com.microfocus.application.automation.tools.settings;

import com.hp.octane.integrations.OctaneConfiguration;
import com.hp.octane.integrations.OctaneSDK;
import com.microfocus.application.automation.tools.model.OctaneServerSettingsModel;
import com.microfocus.application.automation.tools.octane.CIJenkinsServicesImpl;
import com.microfocus.application.automation.tools.octane.Messages;
import com.microfocus.application.automation.tools.octane.configuration.ConfigurationListener;
import com.microfocus.application.automation.tools.octane.configuration.ConfigurationParser;
import com.microfocus.application.automation.tools.octane.configuration.MqmProject;
import hudson.CopyOnWrite;
import hudson.Extension;
import hudson.ExtensionList;
import hudson.XmlFile;
import hudson.model.AbstractProject;
import hudson.model.Item;
import hudson.model.User;
import hudson.security.ACL;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.Builder;
import hudson.util.FormValidation;
import hudson.util.Secret;
import jenkins.model.Jenkins;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.acegisecurity.context.SecurityContext;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.StaplerRequest;

import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * Octane configuration settings
 */

public class OctaneServerSettingsBuilder extends Builder {
	private static final Logger logger = LogManager.getLogger(OctaneServerSettingsBuilder.class);

	@Override
	public OctaneDescriptorImpl getDescriptor() {
		return (OctaneDescriptorImpl) super.getDescriptor();
	}

	public static OctaneDescriptorImpl getOctaneSettingsManager() {
		OctaneDescriptorImpl octaneDescriptor = Jenkins.getInstance().getDescriptorByType(OctaneDescriptorImpl.class);
		if (octaneDescriptor == null) {
			throw new IllegalStateException("failed to obtain Octane plugin descriptor");
		}

		return octaneDescriptor;
	}

	/**
	 * Descriptor for {@link OctaneServerSettingsBuilder}. Used as a singleton. The class is marked as
	 * public so that it can be accessed from views.
	 * <p>
	 * See <tt>src/main/resources/hudson/plugins/hello_world/HelloWorldBuilder/*.jelly</tt> for the
	 * actual HTML fragment for the configuration screen.
	 */
	@Extension
	public static final class OctaneDescriptorImpl extends BuildStepDescriptor<Builder> {

		@CopyOnWrite
		private OctaneServerSettingsModel[] servers;

		private transient Map<String, OctaneConfiguration> octaneConfigurations = new HashMap<>();

		@Override
		protected XmlFile getConfigFile() {
			XmlFile xmlFile = super.getConfigFile();
			//Between 5.1 to 5.2 - migration hp->hpe was done.
			//Old configuration file 'com.hp.application.automation.tools.settings.OctaneServerSettingsBuilder.xml'
			//is replaced by new one 'com.hpe.application.automation.tools.settings.OctaneServerSettingsBuilder.xml'.
			//As well, inside the configuration, there were replaces of hp->hpe
			//if xmlFile is not exist, we will check if configuration file name exist in format of 5.1 version
			//if so, we will copy old configuration to new one with replacements of hp->hpe
			if (!xmlFile.exists()) {
				//try to get from old path
				File oldConfigurationFile = new File(xmlFile.getFile().getPath().replace("hpe", "hp"));
				if (oldConfigurationFile.exists()) {
					try {
						String configuration = FileUtils.readFileToString(oldConfigurationFile);
						String newConfiguration = StringUtils.replace(configuration, ".hp.", ".hpe.");
						FileUtils.writeStringToFile(xmlFile.getFile(), newConfiguration);
						xmlFile = super.getConfigFile();
					} catch (IOException e) {
						logger.error("failed to copy ALM Octane Plugin configuration 5.1 to new 5.2 format : " + e.getMessage());
					}
				}
			}

			return xmlFile;
		}

		public OctaneDescriptorImpl() {
			load();
			if (servers == null) {
				servers = new OctaneServerSettingsModel[0];
			}

			boolean shouldSave = false;
			// defense for non-octane users.Previously, before multi-configuration, octane had only one configuration,
			// even empty. So after moving to multi-configuration, non-octane users have non-valid configuration and
			// will fail to save jenkins configuration as missing valid octane configuration
			if (servers.length == 1 && StringUtils.isEmpty(servers[0].getUiLocation())) {
				servers = new OctaneServerSettingsModel[0];
				shouldSave = true;
			}

			//  upgrade flow to add internal ID to configuration
			for (OctaneServerSettingsModel server : servers) {
				if (server.getInternalId() == null || server.getInternalId().isEmpty()) {
					server.setInternalId(UUID.randomUUID().toString());
					shouldSave = true;
				}
			}
			if (shouldSave) {
				save();
			}
		}

		public void initOctaneClients() {
			for (OctaneServerSettingsModel innerServerConfiguration : servers) {
				OctaneConfiguration octaneConfiguration = new OctaneConfiguration(innerServerConfiguration.getIdentity(), innerServerConfiguration.getLocation(),
						innerServerConfiguration.getSharedSpace());
				octaneConfiguration.setClient(innerServerConfiguration.getUsername());
				octaneConfiguration.setSecret(innerServerConfiguration.getPassword().getPlainText());
				octaneConfigurations.put(innerServerConfiguration.getInternalId(), octaneConfiguration);
				OctaneSDK.addClient(octaneConfiguration, CIJenkinsServicesImpl.class);
			}
		}

		@Override
		public boolean isApplicable(
				@SuppressWarnings("rawtypes") Class<? extends AbstractProject> aClass) {
			// Indicates that this builder can be used with all kinds of project
			// types
			return true;
		}

		/**
		 * This human readable name is used in the configuration screen.
		 */
		@Override
		public String getDisplayName() {
			return "";
		}

		@Override
		public boolean configure(StaplerRequest req, JSONObject formData) throws FormException {
			Object data = formData.get("mqm");
			JSONArray jsonArray = new JSONArray();
			if (data instanceof JSONObject) {
				jsonArray.add(data);
			} else if (data instanceof JSONArray) {
				jsonArray.addAll((JSONArray) data);
			}

			handleDeletedConfigurations(jsonArray);
			for (Object jsonObject : jsonArray) {
				JSONObject json = (JSONObject) jsonObject;
				OctaneServerSettingsModel newModel = req.bindJSON(OctaneServerSettingsModel.class, json);
				String identity = "";
				OctaneServerSettingsModel oldModel;

				if (json.containsKey("showIdentity")) {
					JSONObject showIdentity = (JSONObject) json.get("showIdentity");
					identity = showIdentity.getString("identity");
					validateConfiguration(doCheckInstanceId(identity), "Plugin instance id");
				}

				String internalId = json.getString("internalId");
				validateConfiguration(doCheckUiLocation(json.getString("uiLocation"), internalId), "Location");
				oldModel = getSettingsByInternalId(internalId);
				if (oldModel != null) {
					newModel.setIdentity(identity.isEmpty() ? oldModel.getIdentity() : identity);
					newModel.setInternalId(oldModel.getInternalId());
				}


				if (json.containsKey("maxTimeoutHours")) {
					String sscPollingTimeoutString = json.getString("maxTimeoutHours");
					if (sscPollingTimeoutString != null && !sscPollingTimeoutString.isEmpty()) {
						try {
							long sscPollingTimeout = Long.valueOf(sscPollingTimeoutString);
							newModel.setMaxTimeoutHours(sscPollingTimeout);
						} catch (NumberFormatException e) {
							newModel.setMaxTimeoutHours(0);
						}
					}
				}
				setModel(newModel);
			}

			return super.configure(req, formData);
		}

		private void handleDeletedConfigurations(JSONArray jsonArray) {
			if (servers == null) {
				return;
			}

			Set<OctaneServerSettingsModel> serversToRemove = new LinkedHashSet<>();
			for (OctaneServerSettingsModel server : servers) {
				boolean configFound = false;
				for (Object jsonObj : jsonArray) {
					if (server.getInternalId().equals(((JSONObject) jsonObj).getString("internalId"))) {
						configFound = true;
						break;
					}
				}
				if (!configFound) {
					OctaneConfiguration octaneConfiguration = octaneConfigurations.get(server.getInternalId());
					serversToRemove.add(server);
					if (octaneConfiguration != null) {
						logger.info("Removing client with instance Id: " + server.getIdentity());
						OctaneSDK.removeClient(OctaneSDK.getClientByInstanceId(server.getIdentity()));

					}
				}
			}

			List<OctaneServerSettingsModel> serversToLeave = new ArrayList<>(Arrays.asList(servers));
			for (OctaneServerSettingsModel serverToRemove : serversToRemove) {
				serversToLeave.remove(serverToRemove);
				octaneConfigurations.remove(serverToRemove.getInternalId());
			}
			servers = serversToLeave.toArray(new OctaneServerSettingsModel[0]);

			if (!serversToRemove.isEmpty()) {
				save();
			}
		}

		public void setModel(OctaneServerSettingsModel newModel) {
			//infer uiLocation
			MqmProject mqmProject;
			try {
				mqmProject = ConfigurationParser.parseUiLocation(newModel.getUiLocation());
				newModel.setSharedSpace(mqmProject.getSharedSpace());
				newModel.setLocation(mqmProject.getLocation());
			} catch (FormValidation fv) {
				logger.warn("tested configuration failed on Octane URL parse: " + fv.getMessage(), fv);
			}

			OctaneServerSettingsModel oldModel = getSettingsByInternalId(newModel.getInternalId());
			//  set identity in new model
			if (oldModel == null) {
				if (newModel.getIdentity() == null || newModel.getIdentity().isEmpty()) {
					newModel.setIdentity(UUID.randomUUID().toString());
				}
				if (newModel.getIdentityFrom() == null) {
					newModel.setIdentityFrom(System.currentTimeMillis());
				}
			} else if (oldModel.getIdentity() != null && !oldModel.getIdentity().isEmpty()) {
				newModel.setIdentityFrom(oldModel.getIdentityFrom());
			}

			if (oldModel == null) {
				if (servers == null) {
					servers = new OctaneServerSettingsModel[]{newModel};
				} else {
					if (servers.length == 1 && !servers[0].isValid()) {
						//  replacing the first dummy one
						servers[0] = newModel;
					} else {
						//  adding new one
						OctaneServerSettingsModel[] newServers = new OctaneServerSettingsModel[servers.length + 1];
						System.arraycopy(servers, 0, newServers, 0, servers.length);
						newServers[servers.length] = newModel;
						servers = newServers;
					}
				}
			} else {
				updateServer(servers, newModel, oldModel);
			}
			OctaneConfiguration octaneConfiguration = octaneConfigurations.containsKey(newModel.getInternalId()) ?
					octaneConfigurations.get(newModel.getInternalId()) :
					new OctaneConfiguration(newModel.getIdentity(), newModel.getLocation(), newModel.getSharedSpace());
			octaneConfiguration.setSharedSpace(newModel.getSharedSpace());
			octaneConfiguration.setUrl(newModel.getLocation());
			octaneConfiguration.setClient(newModel.getUsername());
			octaneConfiguration.setSecret(newModel.getPassword().getPlainText());

			if (!octaneConfigurations.containsValue(octaneConfiguration)) {
				octaneConfigurations.put(newModel.getInternalId(), octaneConfiguration);
				OctaneSDK.addClient(octaneConfiguration, CIJenkinsServicesImpl.class);
			}


			if (!newModel.equals(oldModel)) {
				fireOnChanged(newModel, oldModel);
			}

			save();
		}

		private void updateServer(OctaneServerSettingsModel[] servers, OctaneServerSettingsModel newModel, OctaneServerSettingsModel oldModel) {
			if (servers != null) {
				for (int i = 0; i < servers.length; i++) {
					if (newModel.getInternalId().equals(servers[i].getInternalId())) {
						servers[i] = newModel;
						if (!newModel.getIdentity().equals(oldModel.getIdentity())) {
							logger.info("Removing client with instance Id: " + oldModel.getIdentity());
							OctaneSDK.removeClient(OctaneSDK.getClientByInstanceId(octaneConfigurations.get(oldModel.getInternalId()).getInstanceId()));
							octaneConfigurations.remove(newModel.getInternalId());
						}
						break;
					}
				}
			}
		}

		private void fireOnChanged(OctaneServerSettingsModel newConf, OctaneServerSettingsModel oldConf) {
			OctaneSDK.getClients().forEach(octaneClient -> octaneClient.getConfigurationService().notifyChange());
			ExtensionList<ConfigurationListener> listeners = ExtensionList.lookup(ConfigurationListener.class);
			for (ConfigurationListener listener : listeners) {
				try {
					listener.onChanged(newConf, oldConf);
				} catch (ThreadDeath t) {
					throw t;
				} catch (Throwable t) {
					logger.warn(t);
				}
			}
		}

		@SuppressWarnings("unused")
		public FormValidation doTestConnection(@QueryParameter("uiLocation") String uiLocation,
		                                       @QueryParameter("username") String username,
		                                       @QueryParameter("password") String password,
		                                       @QueryParameter("impersonatedUser") String impersonatedUser) {
			MqmProject mqmProject;
			try {
				mqmProject = ConfigurationParser.parseUiLocation(uiLocation);
			} catch (FormValidation fv) {
				logger.warn("tested configuration failed on Octane URL parse: " + fv.getMessage(), fv);
				return fv;
			}

			//  if parse is good, check authentication/authorization
			ConfigurationParser parser = Jenkins.getInstance().getExtensionList(ConfigurationParser.class).iterator().next();
			FormValidation validation = parser.checkConfiguration(mqmProject.getLocation(), mqmProject.getSharedSpace(), username, Secret.fromString(password));

			//  if still good, check Jenkins user permissions
			try {
				SecurityContext preserveContext = impersonate(impersonatedUser);
				if (!Jenkins.getInstance().hasPermission(Item.READ)) {
					logger.warn("tested configuration failed on insufficient Jenkins' user permissions");
					validation = FormValidation.errorWithMarkup(ConfigurationParser.markup("red", Messages.JenkinsUserPermissionsFailure()));
				}
				depersonate(preserveContext);
			} catch (FormValidation fv) {
				logger.warn("tested configuration failed on impersonating Jenkins' user, most likely non existent user provided", fv);
				return fv;
			}

			return validation;
		}

		public OctaneServerSettingsModel[] getServers() {
			return servers;
		}

		public OctaneServerSettingsModel getSettings(String instanceId) {
			if (instanceId == null || instanceId.isEmpty()) {
				throw new IllegalArgumentException("instance ID MUST NOT be null nor empty");
			}

			OctaneServerSettingsModel result = null;
			if (servers != null) {
				for (OctaneServerSettingsModel setting : servers) {
					if (instanceId.equals(setting.getIdentity())) {
						result = setting;
						break;
					}
				}
			}
			return result;
		}

		public OctaneServerSettingsModel getSettingsByInternalId(String internalId) {
			if (internalId == null || internalId.isEmpty()) {
				return null;
			}

			OctaneServerSettingsModel result = null;
			if (servers != null) {
				for (OctaneServerSettingsModel setting : servers) {
					if (internalId.equals(setting.getInternalId())) {
						result = setting;
						break;
					}
				}
			}
			return result;
		}

		private SecurityContext impersonate(String user) throws FormValidation {
			SecurityContext originalContext = null;
			if (user != null && !user.equalsIgnoreCase("")) {
				User jenkinsUser = User.get(user, false, Collections.emptyMap());
				if (jenkinsUser != null) {
					originalContext = ACL.impersonate(jenkinsUser.impersonate());
				} else {
					throw FormValidation.errorWithMarkup(ConfigurationParser.markup("red", Messages.JenkinsUserPermissionsFailure()));
				}
			}
			return originalContext;
		}

		private void depersonate(SecurityContext originalContext) {
			if (originalContext != null) {
				ACL.impersonate(originalContext.getAuthentication());
			}
		}

		public FormValidation doCheckInstanceId(@QueryParameter String value) {
			if (value == null || value.isEmpty()) {
				return FormValidation.error("Plugin Instance Id cannot be empty");
			}

			return FormValidation.ok();
		}

		public FormValidation doCheckUiLocation(@QueryParameter String value, @QueryParameter(value = "internalId") String internalId) {
			FormValidation ret = FormValidation.ok();
			//Relevant only for new server configuration (empty internalId)
			if (!StringUtils.isBlank(internalId)) {
				return ret;
			}
			if (StringUtils.isBlank(value)) {
				ret = FormValidation.error("Location must be set");
				return ret;
			}
			MqmProject mqmProject = null;



			try {
				mqmProject = ConfigurationParser.parseUiLocation(value);

			} catch (Exception e) {
				ret = FormValidation.error("Failed to parse location.");
			}
			for (OctaneServerSettingsModel serverSettingsModel : servers) {
				if (mqmProject != null && serverSettingsModel.getSharedSpace().equals(mqmProject.getSharedSpace()) &&
						serverSettingsModel.getLocation().equals(mqmProject.getLocation())) {
					ret = FormValidation.error("This ALM Octane server configuration was already set.");
					return ret;
				}
			}
			return ret;
		}

		private void validateConfiguration(FormValidation result, String formField) throws FormException {
			if (!result.equals(FormValidation.ok())) {
				throw new FormException("Validation of property '" + formField +  "' in ALM Octane server Configuration failed: " + result.getMessage(), formField);
			}
		}
	}
}
