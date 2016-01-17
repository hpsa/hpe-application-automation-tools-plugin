package com.hp.octane.plugins.jetbrains.teamcity;

/**
 * Created by lazara on 23/12/2015.
 */

import com.hp.octane.plugins.common.bridge.BridgesService;
import com.hp.octane.plugins.common.configuration.ServerConfiguration;
import com.hp.octane.plugins.jetbrains.teamcity.actions.BuildActionsController;
import com.hp.octane.plugins.jetbrains.teamcity.actions.PluginActionsController;
import com.hp.octane.plugins.jetbrains.teamcity.actions.ProjectActionsController;
import com.hp.octane.plugins.jetbrains.teamcity.actions.StatusActionController;
import com.hp.octane.plugins.jetbrains.teamcity.client.TeamCityMqmRestClientFactory;
import com.hp.octane.plugins.jetbrains.teamcity.factories.ModelFactory;
import com.hp.octane.plugins.jetbrains.teamcity.factories.TeamCityModelFactory;
import jetbrains.buildServer.responsibility.BuildTypeResponsibilityFacade;
import jetbrains.buildServer.serverSide.ProjectManager;
import jetbrains.buildServer.serverSide.SBuildServer;
import jetbrains.buildServer.serverSide.ServerExtension;
import jetbrains.buildServer.web.openapi.WebControllerManager;
import org.apache.commons.lang.StringUtils;

import java.util.Date;
import java.util.UUID;
import java.util.logging.Logger;

public class NGAPlugin implements ServerExtension {
    public static final String PLUGIN_NAME = NGAPlugin.class.getSimpleName().toLowerCase();
    private static final Logger logger = Logger.getLogger(NGAPlugin.class.getName());

    private String identity;
    private Long identityFrom;

    // inferred from uiLocation
    private String location="http://localhost:8080";
    private final String PLUGIN_TYPE = "HPE_TEAMCITY_PLUGIN";

    private SBuildServer sBuildServer;
    private ProjectManager projectManager;
    private BuildTypeResponsibilityFacade responsibilityFacade;
    private static NGAPlugin plugin;
    public NGAPlugin(SBuildServer sBuildServer,
                     ProjectManager projectManager,
                     BuildTypeResponsibilityFacade responsibilityFacade,
                     WebControllerManager webControllerManager) {
        logger.info("Init HPE MQM CI Plugin");
        sBuildServer.registerExtension(ServerExtension.class, PLUGIN_NAME, this);
        this.plugin =  this;
        this.sBuildServer = sBuildServer;
        this.projectManager = projectManager;
        this.responsibilityFacade = responsibilityFacade;
//        server.addListener(new BuildEventListener());
        registerControllers(webControllerManager, projectManager, sBuildServer);
        initOPB();
    }

    public SBuildServer getsBuildServer() {
        return sBuildServer;
    }

    public ProjectManager getProjectManager() {
        return projectManager;
    }

    public BuildTypeResponsibilityFacade getResponsibilityFacade() {
        return responsibilityFacade;
    }

    public static NGAPlugin getInstance() {
        return plugin;
    }

    private void registerControllers(WebControllerManager webControllerManager, ProjectManager projectManager,SBuildServer sBuildServer) {
        ModelFactory modelFactory = new TeamCityModelFactory(projectManager);
        webControllerManager.registerController("/octane/jobs/**",
                new PluginActionsController(sBuildServer, projectManager, responsibilityFacade,modelFactory));

        webControllerManager.registerController("/octane/snapshot/**",
                new BuildActionsController(sBuildServer, projectManager, responsibilityFacade,modelFactory));

        webControllerManager.registerController("/octane/structure/**",
                new ProjectActionsController(sBuildServer, projectManager, responsibilityFacade,modelFactory));
        webControllerManager.registerController("/octane/status/**",
                new StatusActionController(sBuildServer, projectManager, responsibilityFacade));
    }

    private void initOPB() {
        if (StringUtils.isEmpty(DummyPluginConfiguration.identity)) {
            DummyPluginConfiguration.identity = UUID.randomUUID().toString();
        }
        if (DummyPluginConfiguration.identityFrom == null || DummyPluginConfiguration.identityFrom == 0) {
            DummyPluginConfiguration.identityFrom = new Date().getTime();
        }
        //BridgesService.getInstance().setMqmRestClientFactory(new TeamCityMqmRestClientFactory());
        BridgesService.getInstance().setCIType(PLUGIN_TYPE);
        BridgesService.getInstance().updateBridge(getServerConfiguration());
    }

    public ServerConfiguration getServerConfiguration() {
        return new ServerConfiguration(
                DummyPluginConfiguration.location,//sBuildServer.getRootUrl(),
                DummyPluginConfiguration.sharedSpace,
                DummyPluginConfiguration.username,
                DummyPluginConfiguration.password,
                DummyPluginConfiguration.impersonatedUser);
    }

}
