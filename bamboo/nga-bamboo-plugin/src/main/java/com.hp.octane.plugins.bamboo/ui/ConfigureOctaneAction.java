package com.hp.octane.plugins.bamboo.ui;

import com.atlassian.bamboo.user.BambooUserManager;
import com.atlassian.bamboo.ww2.BambooActionSupport;
import com.atlassian.sal.api.component.ComponentLocator;
import com.atlassian.sal.api.pluginsettings.PluginSettings;
import com.atlassian.sal.api.pluginsettings.PluginSettingsFactory;
import com.hp.octane.integrations.OctaneSDK;
import com.hp.octane.integrations.dto.configuration.OctaneConfiguration;
import com.hp.octane.integrations.dto.connectivity.OctaneResponse;

import com.hp.octane.plugins.bamboo.api.OctaneConfigurationKeys;
import com.hp.octane.plugins.bamboo.octane.BambooPluginServices;
import org.apache.commons.httpclient.HttpStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;

import java.io.IOException;

public class ConfigureOctaneAction extends BambooActionSupport implements InitializingBean {
	private static final Logger log = LoggerFactory.getLogger(ConfigureOctaneAction.class);

	private final PluginSettingsFactory settingsFactory;

	private String octaneUrl;
	private String accessKey;
	private String apiSecret;
	private String userName;

	public ConfigureOctaneAction(PluginSettingsFactory settingsFactory) {
		this.settingsFactory = settingsFactory;
		readData();
	}

	// TODO internationalize all texts
	@Override
	public void validate() {
		super.validate();

		log.info("validate");

		if (octaneUrl == null || octaneUrl.isEmpty()) {
			addFieldError("octaneUrl", "Octane Instance URL is required.");
		}
		if (accessKey == null || accessKey.isEmpty()) {
			addFieldError("accessKey", "Access Key is required.");
		}
		if (userName != null && !userName.isEmpty()) {
			BambooUserManager userManager = ComponentLocator.getComponent(BambooUserManager.class);
			if (userManager.getBambooUser(userName) == null) {
				addFieldError("userName", "User does not exist");
			}
		}
		if (!getFieldErrors().isEmpty()) {
			addActionError("Configuration is invalid, see fields marked with error.");
			return;
		}
		OctaneConfiguration config = OctaneSDK.getInstance().getConfigurationService().buildConfiguration(octaneUrl, accessKey, apiSecret);
		try {
			OctaneResponse result = OctaneSDK.getInstance().getConfigurationService().validateConfiguration(config);
			if (result.getStatus() == HttpStatus.SC_OK) {
				addActionMessage("Config is valid");
			} else if (result.getStatus() == HttpStatus.SC_UNAUTHORIZED) {
				addActionError("Authentication failed");
			} else if (result.getStatus() == HttpStatus.SC_FORBIDDEN) {
				addActionError(config.getApiKey() + " not authorized to shared space " + config.getSharedSpace());
			} else if (result.getStatus() == HttpStatus.SC_NOT_FOUND) {
				addActionError("Shared space " + config.getSharedSpace() + " does not exist");
			} else {
				addActionError("Validation failed for unkown reason with status " + result.getStatus());
			}
		} catch (IOException e) {
			log.error("Error validating octane config", e);
			addActionError("Connection failed " + e.getMessage());
		}

	}

	public String doEdit() {
		log.info("edit configuration");
		return INPUT;
	}

	public String doSave() {
		log.info("save configuration");
		PluginSettings settings = settingsFactory.createGlobalSettings();
		settings.put(OctaneConfigurationKeys.OCTANE_URL, octaneUrl);
		settings.put(OctaneConfigurationKeys.ACCESS_KEY, accessKey);
		settings.put(OctaneConfigurationKeys.API_SECRET, apiSecret);
		settings.put(OctaneConfigurationKeys.IMPERSONATION_USER, userName);
		addActionMessage("Config updated");
		return SUCCESS;
	}

	public String getUserName() {
		return userName;
	}

	public void setUserName(String username) {
		userName = username;
	}

	public String getOctaneUrl() {
		return octaneUrl;
	}

	public void setOctaneUrl(String octaneUrl) {
		this.octaneUrl = octaneUrl;
	}

	public String getAccessKey() {
		return accessKey;
	}

	public void setAccessKey(String accessKey) {
		this.accessKey = accessKey;
	}

	public String getApiSecret() {
		return apiSecret;
	}

	public void setApiSecret(String apiSecret) {
		this.apiSecret = apiSecret;
	}

	public void afterPropertiesSet() throws Exception {
		try {
			OctaneSDK.init(new BambooPluginServices(settingsFactory), true);
		} catch (IllegalStateException e) {
			// ignore double init
		}
	}

	private void readData() {
		PluginSettings settings = settingsFactory.createGlobalSettings();
		if (settings.get(OctaneConfigurationKeys.OCTANE_URL) != null) {
			octaneUrl = String.valueOf(settings.get(OctaneConfigurationKeys.OCTANE_URL));
		} else {
			octaneUrl = "";
		}
		if (settings.get(OctaneConfigurationKeys.ACCESS_KEY) != null) {
			accessKey = String.valueOf(settings.get(OctaneConfigurationKeys.ACCESS_KEY));
		} else {
			accessKey = "";
		}
		if (settings.get(OctaneConfigurationKeys.API_SECRET) != null) {
			apiSecret = String.valueOf(settings.get(OctaneConfigurationKeys.API_SECRET));
		} else {
			apiSecret = "";
		}
		if (settings.get(OctaneConfigurationKeys.IMPERSONATION_USER) != null) {
			userName = String.valueOf(settings.get(OctaneConfigurationKeys.IMPERSONATION_USER));
		} else {
			userName = "";
		}
	}
}
