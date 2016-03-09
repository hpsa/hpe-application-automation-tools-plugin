package com.hp.octane.plugins.jetbrains.teamcity.actions;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hp.nga.integrations.SDKManager;
import com.hp.nga.integrations.dto.DTOInternalProviderBase;
import com.hp.nga.integrations.dto.configuration.NGAConfiguration;
import com.hp.octane.plugins.jetbrains.teamcity.configuration.NGAConfig;
import com.hp.octane.plugins.jetbrains.teamcity.NGAPlugin;
import com.hp.octane.plugins.jetbrains.teamcity.configuration.ConfigurationService;
import com.hp.octane.plugins.jetbrains.teamcity.utils.ConfigManager;
import jetbrains.buildServer.serverSide.SBuildServer;
import jetbrains.buildServer.web.openapi.PluginDescriptor;
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
	private SBuildServer m_server;
	private PluginDescriptor m_descriptor;

	public ConfigurationActionsController(SBuildServer server, PluginDescriptor descriptor) {
		m_server = server;
		m_descriptor = descriptor;
	}

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
				NGAConfiguration ngaConfiguration = ConfigurationService.buildConfiguration(url, apiKey, secret);

				if (action.equals("test")) {
					returnStr = ConfigurationService.checkConfiguration(ngaConfiguration);
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
		NGAConfig cfg = NGAPlugin.getInstance().getConfig();
		ConfigManager cfgManager = ConfigManager.getInstance(m_descriptor, m_server);

		cfg.setUiLocation(originalUrl);
		cfg.setLocation(ngaConfiguration.getUrl());
		cfg.setSharedSpace(ngaConfiguration.getSharedSpace());
		cfg.setUsername(ngaConfiguration.getApiKey());
		cfg.setSecretPassword(ngaConfiguration.getSecret());
		cfgManager.jaxbObjectToXML(cfg);

		//BridgesService.getInstance().updateBridge(serverConf);

		return "Updated successfully";
	}

	public String reloadConfiguration() {
		try {
			ObjectMapper mapper = new ObjectMapper();
			NGAPlugin ngaPlugin = NGAPlugin.getInstance();
			NGAConfig cfg = ngaPlugin.getConfig();
			return mapper.writeValueAsString(cfg);
		} catch (JsonProcessingException e) {
			logger.log(Level.WARNING, "failed to reload configuration: " + e.getMessage());
			e.printStackTrace();
		}
		return null;
	}
}
