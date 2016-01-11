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

public class PluginRouter implements ServerExtension {
    public static final String PLUGIN_NAME = PluginRouter.class.getSimpleName().toLowerCase();
    private static final Logger logger = Logger.getLogger(PluginRouter.class.getName());
    private SBuildServer sBuildServer;
    private String identity;
    private Long identityFrom;

    // inferred from uiLocation
    private String location="http://localhost:8080";
    private final String PLUGIN_TYPE = "HPE_TEAMCITY_PLUGIN";

    public PluginRouter(SBuildServer server,
                        ProjectManager projectManager,
                        BuildTypeResponsibilityFacade responsibilityFacade,
                        WebControllerManager webControllerManager) {
        logger.info("Init HPE MQM CI Plugin");
        this.sBuildServer = server;
        server.registerExtension(ServerExtension.class, PLUGIN_NAME, this);
//        server.addListener(new BuildEventListener());
        ModelFactory modelFactory = new TeamCityModelFactory(projectManager);


        webControllerManager.registerController("/octane/jobs/**",
                new PluginActionsController(server, projectManager, responsibilityFacade,modelFactory));

        webControllerManager.registerController("/octane/snapshot/**",
                new BuildActionsController(server, projectManager, responsibilityFacade,modelFactory));
        
        webControllerManager.registerController("/octane/structure/**",
                new ProjectActionsController(server, projectManager, responsibilityFacade,modelFactory));
        webControllerManager.registerController("/octane/status/**",
                new StatusActionController(server, projectManager, responsibilityFacade));
        initiPlugin();
    }

    private void initiPlugin() {
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
