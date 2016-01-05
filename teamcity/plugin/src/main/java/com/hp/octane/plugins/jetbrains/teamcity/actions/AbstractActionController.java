package com.hp.octane.plugins.jetbrains.teamcity.actions;

import jetbrains.buildServer.responsibility.BuildTypeResponsibilityFacade;
import jetbrains.buildServer.serverSide.ProjectManager;
import jetbrains.buildServer.serverSide.SBuildServer;

/**
 * Created by lazara on 04/01/2016.
 */
public class AbstractActionController {

    protected final SBuildServer myServer;
    protected final ProjectManager projectManager;
    protected final BuildTypeResponsibilityFacade responsibilityFacade;

    public AbstractActionController(SBuildServer server, ProjectManager projectManager, BuildTypeResponsibilityFacade responsibilityFacade) {
        this.myServer = server;
        this.projectManager = projectManager;
        this.responsibilityFacade = responsibilityFacade;
    }
}
