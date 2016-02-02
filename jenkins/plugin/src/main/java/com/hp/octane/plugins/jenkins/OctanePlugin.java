// (C) Copyright 2003-2015 Hewlett-Packard Development Company, L.P.

package com.hp.octane.plugins.jenkins;

import com.google.inject.Inject;
import com.hp.nga.integrations.api.SDKServicesProvider;
import com.hp.nga.integrations.services.SDKFactory;
import com.hp.octane.plugins.jenkins.bridge.BridgesService;
import com.hp.octane.plugins.jenkins.client.RetryModel;
import com.hp.octane.plugins.jenkins.configuration.ConfigurationListener;
import com.hp.octane.plugins.jenkins.configuration.ConfigurationService;
import com.hp.octane.plugins.jenkins.configuration.MqmProject;
import com.hp.octane.plugins.jenkins.configuration.PredefinedConfiguration;
import com.hp.octane.plugins.jenkins.configuration.PredefinedConfigurationUnmarshaller;
import com.hp.octane.plugins.jenkins.configuration.ServerConfiguration;
import com.hp.octane.plugins.jenkins.events.EventsService;
import hudson.Extension;
import hudson.ExtensionList;
import hudson.Plugin;
import hudson.model.Describable;
import hudson.model.Descriptor;
import hudson.util.FormValidation;
import hudson.util.Scrambler;
import hudson.util.Secret;
import jenkins.model.Jenkins;
import net.sf.json.JSONObject;
import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.StringUtils;
import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.StaplerRequest;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Date;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

public class OctanePlugin extends Plugin implements Describable<OctanePlugin> {
	private static Logger logger = Logger.getLogger(OctanePlugin.class.getName());

	private String identity;
	private Long identityFrom;

	private String uiLocation;
	private String username;
	private Secret secretPassword;
	private String impersonatedUser;

	// inferred from uiLocation
	private String location;
	private String sharedSpace;

	// deprecated, replaced by secretPassword
	private String password;

	public String getIdentity() {
		return identity;
	}

	public Long getIdentityFrom() {
		return identityFrom;
	}

	// identity should not be changed under normal circumstances; this functionality is provided in order to simplify
	// test automation
	public void setIdentity(String identity) throws IOException {
		if (StringUtils.isEmpty(identity)) {
			throw new IllegalArgumentException("Empty identity is not allowed");
		}
		this.identity = identity;
		this.identityFrom = new Date().getTime();
		save();
	}

	@Override
	public void postInitialize() throws IOException {
		load();
		if (StringUtils.isEmpty(identity)) {
			setIdentity(UUID.randomUUID().toString());
		}
		if (identityFrom == null || identityFrom == 0) {
			this.identityFrom = new Date().getTime();
			save();
		}
		if (!StringUtils.isEmpty(password) && secretPassword == null) {
			// migrate from password to secret password
			secretPassword = Secret.fromString(Scrambler.descramble(password));
			password = null;
			save();
		}

		// once the global configuration is saved in UI, all values are initialized
		if (uiLocation == null) {
			File configurationFile = null;
			try {
				File resourceDirectory = new File(getWrapper().baseResourceURL.toURI());
				configurationFile = new File(resourceDirectory, "predefinedConfiguration.xml");
			} catch (URISyntaxException e) {
				logger.log(Level.WARNING, "Unable to convert path of the predefined server configuration file", e);
			}
			PredefinedConfigurationUnmarshaller predefinedConfigurationUnmarshaller = PredefinedConfigurationUnmarshaller.getExtensionInstance();
			if (configurationFile != null && configurationFile.canRead() && predefinedConfigurationUnmarshaller != null) {
				PredefinedConfiguration predefinedConfiguration = predefinedConfigurationUnmarshaller.unmarshall(configurationFile);
				if (predefinedConfiguration != null) {
					configurePlugin(predefinedConfiguration.getUiLocation(), null, null, null);
				}
			}
		}

		//  X Plugin will decide what's it's pattern to provide instance of the implementation
		SDKFactory.init(new CIPluginServicesImpl());
		//  TODO: use sdk object here to init the server internal events and notify the sdk
		SDKServicesProvider sdkServicesProvider = SDKFactory.getSDKServicesProvider();

		//  These ones, once will become part of the SDK, will be hidden from X Plugin and initialized in SDK internally
		EventsService.getExtensionInstance().updateClient(getServerConfiguration());
		BridgesService.getExtensionInstance().updateBridge(getServerConfiguration());
	}

	@Override
	public Descriptor<OctanePlugin> getDescriptor() {
		return new OctanePluginDescriptor();
	}

	public ServerConfiguration getServerConfiguration() {
		return new ServerConfiguration(
				getLocation(),
				getSharedSpace(),
				getUsername(),
				getPassword(),
				getImpersonatedUser());
	}

	private String getUiLocation() {
		return uiLocation;
	}

	private String getLocation() {
		return location;
	}

	private String getSharedSpace() {
		return sharedSpace;
	}

	private String getUsername() {
		return username;
	}

	private String getPassword() {
		return Secret.toString(secretPassword);
	}

	public String getImpersonatedUser() {
		return impersonatedUser;
	}

	public void configurePlugin(String uiLocation, String username, String password, String impersonatedUser) throws IOException {
		ServerConfiguration oldConfiguration = getServerConfiguration();
		String oldUiLocation = this.uiLocation;

		this.uiLocation = uiLocation;
		this.username = username;
		this.secretPassword = Secret.fromString(password);
		this.impersonatedUser = impersonatedUser;
		try {
			MqmProject mqmProject = ConfigurationService.parseUiLocation(uiLocation);
			location = mqmProject.getLocation();
			sharedSpace = mqmProject.getSharedSpace();
		} catch (FormValidation ex) {
			// consider plugin unconfigured
			logger.warning("invalid configuration submitted; processing failed with error: " + ex.getMessage());
			location = null;
			sharedSpace = null;
		}

		ServerConfiguration newConfiguration = getServerConfiguration();
		if (!oldConfiguration.equals(newConfiguration)) {
			save();
			fireOnChanged(newConfiguration, oldConfiguration);
		} else if (!ObjectUtils.equals(uiLocation, oldUiLocation)) {
			// no change in actual connection parameters, only user interfacing value has changed
			save();
		}
	}

	private void fireOnChanged(ServerConfiguration configuration, ServerConfiguration oldConfiguration) {
		ExtensionList<ConfigurationListener> listeners = ExtensionList.lookup(ConfigurationListener.class);
		for (ConfigurationListener listener : listeners) {
			try {
				listener.onChanged(configuration, oldConfiguration);
			} catch (ThreadDeath t) {
				throw t;
			} catch (Throwable t) {
				logger.log(Level.WARNING, null, t);
			}
		}
	}

	@Extension
	public static final class OctanePluginDescriptor extends Descriptor<OctanePlugin> {

		private OctanePlugin octanePlugin;

		@Inject
		private ConfigurationService configurationService;

		@Inject
		private RetryModel retryModel;

		public OctanePluginDescriptor() {
			octanePlugin = Jenkins.getInstance().getPlugin(OctanePlugin.class);
		}

		@Override
		public boolean configure(StaplerRequest req, JSONObject formData) throws FormException {
			try {
				JSONObject mqmData = formData.getJSONObject("mqm"); // NON-NLS
				octanePlugin.configurePlugin(mqmData.getString("uiLocation"), // NON-NLS
						mqmData.getString("username"), // NON-NLS
						mqmData.getString("password"), mqmData.getString("impersonatedUser")); // NON-NLS
				return true;
			} catch (IOException e) {
				throw new FormException(e, Messages.ConfigurationSaveFailed());
			}
		}

		public FormValidation doCheckUiLocation(@QueryParameter String value) {
			if (value.isEmpty()) {
				return FormValidation.error(Messages.ConfigurationUrlNotSpecified());
			}
			try {
				ConfigurationService.parseUiLocation(value);
				return FormValidation.ok();
			} catch (FormValidation ex) {
				return ex;
			}
		}

		public FormValidation doTestGlobalConnection(@QueryParameter("uiLocation") String uiLocation,
		                                             @QueryParameter("username") String username,
		                                             @QueryParameter("password") String password, @QueryParameter("impersonatedUser") String impersonatedUser) {
			MqmProject mqmProject;
			try {
				mqmProject = ConfigurationService.parseUiLocation(uiLocation);
			} catch (FormValidation ex) {
				// location syntactically incorrect, no need to check connectivity
				return ex;
			}
			FormValidation validation = configurationService.checkConfiguration(mqmProject.getLocation(),
					mqmProject.getSharedSpace(), username, password);
			if (validation.kind == FormValidation.Kind.OK &&
					uiLocation.equals(octanePlugin.getUiLocation()) &&
					username.equals(octanePlugin.getUsername()) &&
					password.equals(octanePlugin.getPassword())) {
				retryModel.success();
			}

			return validation;
		}

		@Override
		public String getDisplayName() {
			return Messages.PluginName();
		}

		public String getUiLocation() {
			return octanePlugin.getUiLocation();
		}

		public String getLocation() {
			return octanePlugin.getLocation();
		}

		public String getSharedSpace() {
			return octanePlugin.getSharedSpace();
		}

		public String getUsername() {
			return octanePlugin.getUsername();
		}

		public String getPassword() {
			return octanePlugin.getPassword();
		}

		public String getImpersonatedUser() {
			return octanePlugin.getImpersonatedUser();
		}
	}
}
