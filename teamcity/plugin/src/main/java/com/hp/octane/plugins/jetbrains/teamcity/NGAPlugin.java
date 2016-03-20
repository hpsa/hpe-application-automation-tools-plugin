package com.hp.octane.plugins.jetbrains.teamcity;

/**
 * Created by lazara on 23/12/2015.
 */

import com.hp.nga.integrations.SDKManager;
import com.hp.octane.plugins.jetbrains.teamcity.actions.ConfigurationActionsController;
import com.hp.octane.plugins.jetbrains.teamcity.actions.DynamicController;
import com.hp.octane.plugins.jetbrains.teamcity.configuration.NGAConfig;
import com.hp.octane.plugins.jetbrains.teamcity.configuration.TCConfigurationService;
import jetbrains.buildServer.responsibility.BuildTypeResponsibilityFacade;
import jetbrains.buildServer.serverSide.ProjectManager;
import jetbrains.buildServer.serverSide.SBuildServer;
import jetbrains.buildServer.serverSide.ServerExtension;
import jetbrains.buildServer.serverSide.settings.ProjectSettingsManager;
import jetbrains.buildServer.web.openapi.PluginDescriptor;
import jetbrains.buildServer.web.openapi.WebControllerManager;

import java.util.Date;
import java.util.UUID;
import java.util.logging.Logger;

public class NGAPlugin implements ServerExtension {
	public static final String PLUGIN_NAME = NGAPlugin.class.getSimpleName().toLowerCase();
	private static final Logger logger = Logger.getLogger(NGAPlugin.class.getName());

	private ProjectManager projectManager;
	private static NGAPlugin plugin;
	private NGAConfig config;

	public NGAPlugin(SBuildServer sBuildServer,
	                 ProjectManager projectManager,
	                 BuildTypeResponsibilityFacade responsibilityFacade,
	                 WebControllerManager webControllerManager,
	                 ProjectSettingsManager projectSettingsManager,
	                 PluginDescriptor pluginDescriptor) {
		logger.info("Init HPE NGA CI Plugin");
		sBuildServer.registerExtension(ServerExtension.class, PLUGIN_NAME, this);
		this.plugin = this;
		this.projectManager = projectManager;
		registerControllers(webControllerManager, sBuildServer, pluginDescriptor);
		TCConfigurationService.init(pluginDescriptor, sBuildServer);
		config = TCConfigurationService.getInstance().readConfig();

		ensureServerInstanceID();
		SDKManager.init(new TeamCityPluginServicesImpl(), true);
	}

	public ProjectManager getProjectManager() {
		return projectManager;
	}

	public static NGAPlugin getInstance() {
		return plugin;
	}

	private void registerControllers(WebControllerManager webControllerManager, SBuildServer sBuildServer, PluginDescriptor pluginDescriptor) {
		webControllerManager.registerController("/nga/**", new DynamicController());
		webControllerManager.registerController("/octane-rest/**", new ConfigurationActionsController(sBuildServer, pluginDescriptor));
	}

	private void ensureServerInstanceID() {
		String identity = config.getIdentity();
		if (identity == null || identity.equals("")) {
			config.setIdentity(UUID.randomUUID().toString());
			config.setIdentityFrom(String.valueOf(new Date().getTime()));
			TCConfigurationService.getInstance().saveConfig(config);
		}
	}

	public NGAConfig getConfig() {
		return config;
	}
}
