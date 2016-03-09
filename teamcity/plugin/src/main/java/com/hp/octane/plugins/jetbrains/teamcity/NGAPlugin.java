package com.hp.octane.plugins.jetbrains.teamcity;

/**
 * Created by lazara on 23/12/2015.
 */

import com.hp.nga.integrations.SDKManager;
import com.hp.octane.plugins.jetbrains.teamcity.actions.ConfigurationActionsController;
import com.hp.octane.plugins.jetbrains.teamcity.actions.DynamicController;
import com.hp.octane.plugins.jetbrains.teamcity.configuration.NGAConfig;
import com.hp.octane.plugins.jetbrains.teamcity.utils.ConfigManager;
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
	private final CIPluginServicesImpl ciPluginService;

	private SBuildServer sBuildServer;
	private ProjectManager projectManager;
	private BuildTypeResponsibilityFacade responsibilityFacade;

	private static NGAPlugin plugin;
	private PluginDescriptor descriptor;
	private NGAConfig config;
	private ConfigManager configManager;

	public SBuildServer getsBuildServer() {
		return sBuildServer;
	}

	public NGAPlugin(SBuildServer sBuildServer,
	                 ProjectManager projectManager,
	                 BuildTypeResponsibilityFacade responsibilityFacade,
	                 WebControllerManager webControllerManager,
	                 ProjectSettingsManager projectSettingsManager,
	                 PluginDescriptor pluginDescriptor) {
		logger.info("Init HPE MQM CI Plugin");
		sBuildServer.registerExtension(ServerExtension.class, PLUGIN_NAME, this);
		this.plugin = this;
		descriptor = pluginDescriptor;
		this.sBuildServer = sBuildServer;
		this.projectManager = projectManager;
		this.responsibilityFacade = responsibilityFacade;
		registerControllers(webControllerManager, projectManager, sBuildServer, projectSettingsManager, pluginDescriptor);
		configManager = ConfigManager.getInstance(descriptor, sBuildServer);
		config = configManager.jaxbXMLToObject();
		this.ciPluginService = new CIPluginServicesImpl();

		SDKManager.init(ciPluginService, true);
		initOPB();
	}

	public ProjectManager getProjectManager() {
		return projectManager;
	}

	public static NGAPlugin getInstance() {
		return plugin;
	}

	private void registerControllers(WebControllerManager webControllerManager, ProjectManager projectManager, SBuildServer sBuildServer, ProjectSettingsManager projectSettingsManager, PluginDescriptor pluginDescriptor) {
		webControllerManager.registerController("/nga/**", new DynamicController());
		webControllerManager.registerController("/octane-rest/**", new ConfigurationActionsController(sBuildServer, pluginDescriptor));
	}

	private void initOPB() {
		String identity = config.getIdentity();
		if (identity == null || identity.equals("")) {
			identity = UUID.randomUUID().toString();
			String newidentityFrom = String.valueOf(new Date().getTime());
			config.setIdentity(identity);
			config.setIdentityFrom(newidentityFrom);
			configManager.jaxbObjectToXML(config);
		}

		//BridgeService.getInstance().updateBridge(ciPluginService.getNGAConfiguration());
	}

	public NGAConfig getConfig() {
		return config;
	}
}
