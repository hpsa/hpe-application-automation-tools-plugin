package com.hp.octane.plugins.jetbrains.teamcity.actions;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hp.octane.integrations.OctaneSDK;
import com.hp.octane.integrations.dto.configuration.OctaneConfiguration;
import com.hp.octane.plugins.jetbrains.teamcity.configuration.OctaneConfigStructure;
import com.hp.octane.plugins.jetbrains.teamcity.OctaneTeamCityPlugin;
import com.hp.octane.plugins.jetbrains.teamcity.configuration.TCConfigurationService;
import jetbrains.buildServer.serverSide.SBuildServer;
import org.jetbrains.annotations.NotNull;
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
	private OctaneTeamCityPlugin octaneTeamCityPlugin;
	@Autowired
	private TCConfigurationService configurationService;

	private ConfigurationActionsController(@NotNull SBuildServer buildServer) {
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
				OctaneConfiguration octaneConfiguration = octaneTeamCityPlugin.getOctaneSDK().getConfigurationService().buildConfiguration(url, apiKey, secret);

				if (action.equals("test")) {
					returnStr = configurationService.checkConfiguration(octaneConfiguration);
				} else if (action.equals("save")) {
					returnStr = updateConfiguration(octaneConfiguration, url);
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

	public String updateConfiguration(OctaneConfiguration octaneConfiguration, String originalUrl) {
		OctaneConfigStructure cfg = octaneTeamCityPlugin.getConfig();
		cfg.setUiLocation(originalUrl);
		cfg.setLocation(octaneConfiguration.getUrl());
		cfg.setSharedSpace(octaneConfiguration.getSharedSpace());
		cfg.setUsername(octaneConfiguration.getApiKey());
		cfg.setSecretPassword(octaneConfiguration.getSecret());
		configurationService.saveConfig(cfg);

		octaneTeamCityPlugin.getOctaneSDK().getConfigurationService().notifyChange(octaneConfiguration);

		return "Updated successfully";
	}

	public String reloadConfiguration() {
		try {
			ObjectMapper mapper = new ObjectMapper();
			OctaneConfigStructure cfg = octaneTeamCityPlugin.getConfig();
			return mapper.writeValueAsString(cfg);
		} catch (JsonProcessingException e) {
			logger.log(Level.WARNING, "failed to reload configuration: " + e.getMessage());
			e.printStackTrace();
		}
		return null;
	}
}
