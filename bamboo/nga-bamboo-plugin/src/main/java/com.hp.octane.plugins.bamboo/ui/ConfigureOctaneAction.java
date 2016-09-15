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
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;

import java.io.IOException;

public class ConfigureOctaneAction extends BambooActionSupport implements InitializingBean {
    private static final long serialVersionUID = 7624086549210664621L;
    private String ngaUrl;
    private String apiKey;
    private String apiSecret;
    private String userToUse;
    private PluginSettingsFactory settingsFactory;

    private static final Logger log = LoggerFactory.getLogger(ConfigureOctaneAction.class);

    public ConfigureOctaneAction() {
        this(ComponentLocator.getComponent(PluginSettingsFactory.class));

    }

    public ConfigureOctaneAction(PluginSettingsFactory settingsFactory) {
        this.settingsFactory = settingsFactory;
        PluginSettings settings = settingsFactory.createGlobalSettings();
        // TODO remove hard-coded default values
        settings.put(OctaneConfigurationKeys.NGA_URL, "http://LAZARA4.emea.hpqcorp.net:8080/ui/?admin&p=2001/1002#/");
        settings.put(OctaneConfigurationKeys.API_KEY, "ayellet.lazar@hpe.com");
        settings.put(OctaneConfigurationKeys.API_SECRET, "+b0abee805fc88417J");
        settings.put(OctaneConfigurationKeys.USER_TO_USE, "lazara");
    }

    // TODO internationalize all texts
    @Override
    public void validate() {
        super.validate();

        log.info("validate");

        if (StringUtils.isBlank(apiKey)) {
            addFieldError("apiKey", "API key is required.");
        }
        if (StringUtils.isBlank(apiSecret)) {
            addFieldError("apiSecret", "API Secret is required.");
        }
        if (StringUtils.isBlank(ngaUrl)) {
            addFieldError("ngaUrl", "Octane Instance URL is required.");
        }

        if (StringUtils.isBlank(userToUse)) {
            addFieldError("userToUse", "Username to use is required.");
        }
        BambooUserManager userManager = ComponentLocator.getComponent(BambooUserManager.class);
        if (userManager.getBambooUser(userToUse) == null) {
            addFieldError("userToUse", "User does not exist");
        }
        if (!getFieldErrors().isEmpty()) {
            addActionError("Configuration is invalid, see fields marked with error.");
            return;
        }
        OctaneConfiguration config = OctaneSDK.getInstance().getConfigurationService().buildConfiguration(ngaUrl,
                apiKey, apiSecret);
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
        PluginSettings settings = settingsFactory.createGlobalSettings();
        setNgaUrl(String.valueOf(settings.get(OctaneConfigurationKeys.NGA_URL)));
        setApiKey(String.valueOf(settings.get(OctaneConfigurationKeys.API_KEY)));
        setApiSecret(String.valueOf(settings.get(OctaneConfigurationKeys.API_SECRET)));
        setUserToUse(String.valueOf(settings.get(OctaneConfigurationKeys.USER_TO_USE)));
        return INPUT;
    }

    public void setUserToUse(String username) {
        userToUse = username;
    }

    public String doSave() {
        log.info("save configuration");
        PluginSettings settings = settingsFactory.createGlobalSettings();
        settings.put(OctaneConfigurationKeys.NGA_URL, getNgaUrl());
        settings.put(OctaneConfigurationKeys.API_KEY, getApiKey());
        settings.put(OctaneConfigurationKeys.API_SECRET, getApiSecret());
        settings.put(OctaneConfigurationKeys.USER_TO_USE, getUserToUse());
        addActionMessage("Config updated");
        return SUCCESS;
    }

    public String getUserToUse() {
        return userToUse;
    }

    public String getNgaUrl() {
        return ngaUrl;
    }

    public void setNgaUrl(String ngaUrl) {
        this.ngaUrl = ngaUrl;
    }

    public String getApiKey() {
        return apiKey;
    }

    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
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

}
