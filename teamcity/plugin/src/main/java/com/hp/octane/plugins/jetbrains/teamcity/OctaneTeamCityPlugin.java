package com.hp.octane.plugins.jetbrains.teamcity;

/**
 * Created by lazara on 23/12/2015.
 */

import com.hp.octane.integrations.OctaneSDK;
import com.hp.octane.plugins.jetbrains.teamcity.actions.ConfigurationActionsController;
import com.hp.octane.plugins.jetbrains.teamcity.actions.GenericOctaneActionsController;
import com.hp.octane.plugins.jetbrains.teamcity.configuration.OctaneConfigStructure;
import com.hp.octane.plugins.jetbrains.teamcity.configuration.TCConfigurationService;
import jetbrains.buildServer.serverSide.ProjectManager;
import jetbrains.buildServer.serverSide.SBuildServer;
import jetbrains.buildServer.serverSide.ServerExtension;
import jetbrains.buildServer.web.openapi.PluginDescriptor;
import jetbrains.buildServer.web.openapi.WebControllerManager;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.PostConstruct;
import java.util.Date;
import java.util.UUID;
import java.util.logging.Logger;

public class OctaneTeamCityPlugin implements ServerExtension {
	public static final String PLUGIN_NAME = OctaneTeamCityPlugin.class.getSimpleName().toLowerCase();
	private static final Logger logger = Logger.getLogger(OctaneTeamCityPlugin.class.getName());

	@Autowired
	private ProjectManager projectManager;
	@Autowired
	private SBuildServer buildServer;
	@Autowired
	private TeamCityPluginServicesImpl pluginServices;
	@Autowired
	private GenericOctaneActionsController genericController;
	@Autowired
	private ConfigurationActionsController configurationController;
	@Autowired
	private TCConfigurationService configurationService;
	@Autowired
	private PluginDescriptor pluginDescriptor;
	@Autowired
	private WebControllerManager webControllerManager;

	//  [YG] TODO: move this config cache to the configuration service
	private OctaneConfigStructure config;
	private OctaneSDK octaneSDK;

	@PostConstruct
	private void initPlugin() {
		logger.info("Init HPE Octane CI Plugin");
		buildServer.registerExtension(ServerExtension.class, PLUGIN_NAME, this);
		registerControllers();
		config = configurationService.readConfig();

		ensureServerInstanceID();
		octaneSDK = OctaneSDK.init(pluginServices, true);
	}

	public ProjectManager getProjectManager() {
		return projectManager;
	}

	public PluginDescriptor getDescriptor() {
		return pluginDescriptor;
	}

	public OctaneConfigStructure getConfig() {
		return config;
	}

	public OctaneSDK getOctaneSDK() {
		return octaneSDK;
	}

	private void registerControllers() {
		webControllerManager.registerController("/nga/**", genericController);
		webControllerManager.registerController("/octane-rest/**", configurationController);
	}

	private void ensureServerInstanceID() {
		String identity = config.getIdentity();
		if (identity == null || identity.equals("")) {
			config.setIdentity(UUID.randomUUID().toString());
			config.setIdentityFrom(String.valueOf(new Date().getTime()));
			configurationService.saveConfig(config);
		}
	}
}
