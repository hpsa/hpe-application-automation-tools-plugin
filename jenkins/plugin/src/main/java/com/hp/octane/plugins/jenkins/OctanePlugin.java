// (C) Copyright 2003-2015 Hewlett-Packard Development Company, L.P.

package com.hp.octane.plugins.jenkins;

import com.google.inject.Inject;
import com.hp.octane.plugins.jenkins.client.RetryModel;
import com.hp.octane.plugins.jenkins.configuration.ConfigurationListener;
import com.hp.octane.plugins.jenkins.configuration.ConfigurationService;
import com.hp.octane.plugins.jenkins.configuration.ServerConfiguration;
import com.hp.octane.plugins.jenkins.notifications.EventDispatcher;
import hudson.Extension;
import hudson.ExtensionList;
import hudson.Plugin;
import hudson.model.Describable;
import hudson.model.Descriptor;
import hudson.util.FormValidation;
import hudson.util.Scrambler;
import jenkins.model.Jenkins;
import net.sf.json.JSONObject;
import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.StaplerRequest;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

public class OctanePlugin extends Plugin implements Describable<OctanePlugin> {

	private static Logger logger = Logger.getLogger(OctanePlugin.class.getName());

	private String identity;

	private String location;
	private String domain;
	private String project;
	private String username;
	private String password;

	public String getIdentity() {
		return identity;
	}

	@Override
	public void postInitialize() throws IOException {
		load();
		if (identity == null) {
			this.identity = UUID.randomUUID().toString();
			save();
		}

		EventDispatcher.updateClient(getServerConfiguration());
	}

	@Override
	public Descriptor<OctanePlugin> getDescriptor() {
		return new OctanePluginDescriptor();
	}

	public ServerConfiguration getServerConfiguration() {
		return new ServerConfiguration(
				getLocation(),
				getDomain(),
				getProject(),
				getUsername(),
				getPassword());
	}

	private String getLocation() {
		return location;
	}

	private String getDomain() {
		return domain;
	}

	private String getProject() {
		return project;
	}

	private String getUsername() {
		return username;
	}

	private String getPassword() {
		return Scrambler.descramble(password);
	}

	private void configurePlugin(JSONObject formData) throws IOException {
		ServerConfiguration oldConfiguration = getServerConfiguration();
		location = (String) formData.get("location");
		domain = (String) formData.get("domain");
		project = (String) formData.get("project");
		username = (String) formData.get("username");
		password = Scrambler.scramble((String) formData.get("password"));

		ServerConfiguration newConfiguration = getServerConfiguration();
		if (!oldConfiguration.equals(newConfiguration)) {
			save();
			fireOnChanged(newConfiguration, oldConfiguration);
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
				octanePlugin.configurePlugin(formData.getJSONObject("mqm")); // NON-NLS
				return true;
			} catch (IOException e) {
				throw new FormException(e, Messages.ConfigurationSaveFailed());
			}
		}

		public FormValidation doCheckLocation(@QueryParameter String value) {
			if (value.isEmpty()) {
				return FormValidation.error(Messages.ConfigurationUrlNotSpecified());
			}
			try {
				new URL(value);
				return FormValidation.ok();
			} catch (MalformedURLException e) {
				return FormValidation.error(Messages.ConfigurationUrInvalid());
			}
		}

		public FormValidation doCheckDomain(@QueryParameter String value) {
			if (value.isEmpty()) {
				return FormValidation.error(Messages.ConfigurationDomainNotSpecified());
			} else {
				return FormValidation.ok();
			}
		}

		public FormValidation doCheckProject(@QueryParameter String value) {
			if (value.isEmpty()) {
				return FormValidation.error(Messages.ConfigurationProjectNotSpecified());
			} else {
				return FormValidation.ok();
			}
		}

		public FormValidation doTestGlobalConnection(@QueryParameter("location") String location,
		                                             @QueryParameter("domain") String domain,
		                                             @QueryParameter("project") String project,
		                                             @QueryParameter("username") String username,
		                                             @QueryParameter("password") String password) {
			FormValidation validation = configurationService.checkConfiguration(location, domain, project, username, password);
			if (validation.kind == FormValidation.Kind.OK &&
					location.equals(octanePlugin.getLocation()) &&
					domain.equals(octanePlugin.getDomain()) &&
					project.equals(octanePlugin.getProject()) &&
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

		public String getLocation() {
			return octanePlugin.getLocation();
		}

		public String getDomain() {
			return octanePlugin.getDomain();
		}

		public String getProject() {
			return octanePlugin.getProject();
		}

		public String getUsername() {
			return octanePlugin.getUsername();
		}

		public String getPassword() {
			return octanePlugin.getPassword();
		}
	}
}
