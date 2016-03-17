package com.hp.octane.plugins.jetbrains.teamcity.actions;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hp.nga.integrations.SDKManager;
import com.hp.nga.integrations.api.ConfigurationService;
import com.hp.nga.integrations.dto.configuration.NGAConfiguration;
import com.hp.octane.plugins.jetbrains.teamcity.configuration.NGAConfigStructure;
import com.hp.octane.plugins.jetbrains.teamcity.NGAPlugin;
import com.hp.octane.plugins.jetbrains.teamcity.configuration.TCConfigurationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.Controller;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by lazara on 14/02/2016.
 */

public class ConfigurationActionsController implements Controller {
	private static final Logger logger = Logger.getLogger(ConfigurationActionsController.class.getName());

	@Autowired
	private NGAPlugin ngaPlugin;
	@Autowired
	private TCConfigurationService configurationService;

	@Override
	public ModelAndView handleRequest(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) {
		String returnStr = "";
		String action = httpServletRequest.getParameter("action");

		if (action == null || action.equals("")) {
			returnStr = reloadConfiguration();
		} else {
			try {
				String url = httpServletRequest.getParameter("server");
				String apiKey = httpServletRequest.getParameter("username1");
				String secret = httpServletRequest.getParameter("password1");
				NGAConfiguration ngaConfiguration = SDKManager.getService(ConfigurationService.class).buildConfiguration(url, apiKey, secret);

				if (action.equals("test")) {
					returnStr = configurationService.checkConfiguration(ngaConfiguration);
				} else if (action.equals("save")) {
					returnStr = updateConfiguration(ngaConfiguration, url);
				}
			} catch (Exception e) {
				returnStr = e.getMessage();
				e.printStackTrace();
			}
		}

		PrintWriter writer;
		try {
			writer = httpServletResponse.getWriter();
			writer.write(returnStr);
		} catch (IOException e) {
			logger.warning(e.getMessage());
		}
		return null;
	}

	public String updateConfiguration(NGAConfiguration ngaConfiguration, String originalUrl) {
		NGAConfigStructure cfg = ngaPlugin.getConfig();
		cfg.setUiLocation(originalUrl);
		cfg.setLocation(ngaConfiguration.getUrl());
		cfg.setSharedSpace(ngaConfiguration.getSharedSpace());
		cfg.setUsername(ngaConfiguration.getApiKey());
		cfg.setSecretPassword(ngaConfiguration.getSecret());
		configurationService.saveConfig(cfg);

		SDKManager.getService(ConfigurationService.class).notifyChange(ngaConfiguration);

		return "Updated successfully";
	}

	public String reloadConfiguration() {
		try {
			ObjectMapper mapper = new ObjectMapper();
			NGAConfigStructure cfg = ngaPlugin.getConfig();
			return mapper.writeValueAsString(cfg);
		} catch (JsonProcessingException e) {
			logger.log(Level.WARNING, "failed to reload configuration: " + e.getMessage());
			e.printStackTrace();
		}
		return null;
	}
}
