package com.hp.octane.plugins.bamboo.ui;

import org.acegisecurity.AccessDeniedException;
import com.atlassian.bamboo.security.BambooPermissionManager;
import com.atlassian.bamboo.ww2.BambooActionSupport;
import com.atlassian.sal.api.pluginsettings.PluginSettings;
import com.atlassian.sal.api.pluginsettings.PluginSettingsFactory;
import com.hp.octane.integrations.OctaneSDK;
import com.hp.octane.plugins.bamboo.api.OctaneConfigurationKeys;
import com.hp.octane.plugins.bamboo.octane.BambooPluginServices;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;

import java.util.UUID;

public class ConfigureOctaneAction extends BambooActionSupport implements InitializingBean {
	private static final Logger logger = LoggerFactory.getLogger(ConfigureOctaneAction.class);

	private final PluginSettingsFactory settingsFactory;

	private String octaneUrl;
	private String accessKey;
	private String apiSecret;
	private String userName;
	private String uuid;

	public ConfigureOctaneAction(PluginSettingsFactory settingsFactory, BambooPermissionManager bambooPermissionManager) {
	    this.setBambooPermissionManager(bambooPermissionManager);
		this.settingsFactory = settingsFactory;
		readData();
	}

	public String doEdit() {
        logger.info("edit configuration");
        if(!this.hasAdminPermission()){
            logger.error("Access Denied, no admin permissions.");
            throw new AccessDeniedException(null);
        }
		return INPUT;
	}

	public String doSave() {
		logger.info("save configuration");
        if(!this.hasAdminPermission()){
            logger.error("Access Denied, no admin permissions.");
            throw new AccessDeniedException(null);
        }

		PluginSettings settings = settingsFactory.createGlobalSettings();
		settings.put(OctaneConfigurationKeys.OCTANE_URL, octaneUrl);
		settings.put(OctaneConfigurationKeys.ACCESS_KEY, accessKey);
		settings.put(OctaneConfigurationKeys.API_SECRET, apiSecret);
		settings.put(OctaneConfigurationKeys.IMPERSONATION_USER, userName);
		addActionMessage("Configuration updated successfully");
		OctaneSDK.getInstance().getConfigurationService().notifyChange();
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

	public String getUuid() {
		return uuid;
	}

	public void setUuid(String uuid) {
		this.uuid = uuid;
	}

	public void afterPropertiesSet() throws Exception {
		try {
			OctaneSDK.init(new BambooPluginServices(settingsFactory), true);
		} catch (IllegalStateException ise) {
			logger.warn("failed to init SDK, more than a single init?", ise);
		}
	}

	private void readData() {
		PluginSettings settings = settingsFactory.createGlobalSettings();
		if (settings.get(OctaneConfigurationKeys.UUID) != null) {
			uuid = String.valueOf(settings.get(OctaneConfigurationKeys.UUID));
		} else {
			// generate new UUID
			uuid = UUID.randomUUID().toString();
			settings.put(OctaneConfigurationKeys.UUID, uuid);
		}
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
