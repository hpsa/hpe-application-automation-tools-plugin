package com.hp.octane.plugins.jetbrains.teamcity;

/**
 * Created by lazara on 23/12/2015.
 */

import com.hp.octane.plugins.jetbrains.teamcity.actions.BuildActionsController;
import com.hp.octane.plugins.jetbrains.teamcity.actions.PluginActionsController;
import com.hp.octane.plugins.jetbrains.teamcity.actions.ProjectActionsController;
import com.hp.octane.plugins.jetbrains.teamcity.factories.ModelFactory;
import com.hp.octane.plugins.jetbrains.teamcity.factories.TeamCityModelFactory;
import jetbrains.buildServer.responsibility.BuildTypeResponsibilityFacade;
import jetbrains.buildServer.serverSide.ProjectManager;
import jetbrains.buildServer.serverSide.SBuildServer;
import jetbrains.buildServer.serverSide.ServerExtension;
import jetbrains.buildServer.web.openapi.WebControllerManager;

public class PluginRouter implements ServerExtension {
    public static final String PLUGIN_NAME = PluginRouter.class.getSimpleName().toLowerCase();

    public PluginRouter(SBuildServer server,
                        ProjectManager projectManager,
                        BuildTypeResponsibilityFacade responsibilityFacade,
                        WebControllerManager webControllerManager) {

        server.registerExtension(ServerExtension.class, PLUGIN_NAME, this);
//        server.addListener(new BuildEventListener());
        ModelFactory modelFactory = new TeamCityModelFactory(projectManager);
        webControllerManager.registerController("/octane/jobs/**",
                new PluginActionsController(server, projectManager, responsibilityFacade,modelFactory));

        webControllerManager.registerController("/octane/snapshot/**",
                new BuildActionsController(server, projectManager, responsibilityFacade,modelFactory));

        webControllerManager.registerController("/octane/structure/**",
                new ProjectActionsController(server, projectManager, responsibilityFacade,modelFactory));
    }
}
