package com.hp.octane.plugins.jetbrains.teamcity.actions;


import com.hp.octane.plugins.jetbrains.teamcity.factories.ModelFactory;
import jetbrains.buildServer.responsibility.BuildTypeResponsibilityFacade;
import jetbrains.buildServer.serverSide.ProjectManager;
import jetbrains.buildServer.serverSide.SBuildServer;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


public class PluginActionsController extends AbstractActionController {

    public PluginActionsController(final SBuildServer server, ProjectManager manager,
                                   BuildTypeResponsibilityFacade descriptor) {
    }

    @Override
    protected Object buildResults(HttpServletRequest request, HttpServletResponse response) {
        return ModelFactory.CreateProjectList();
    }
}