package com.hp.octane.plugins.jetbrains.teamcity.actions;

import jetbrains.buildServer.responsibility.BuildTypeResponsibilityFacade;
import jetbrains.buildServer.serverSide.ProjectManager;
import jetbrains.buildServer.serverSide.SBuildServer;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.Controller;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Created by lazara on 27/12/2015.
 */
public class BuildActionsController implements Controller {
    private final SBuildServer myServer;
    private final ProjectManager projectManager;
    private final BuildTypeResponsibilityFacade responsibilityFacade;

    public BuildActionsController(SBuildServer server, ProjectManager projectManager, BuildTypeResponsibilityFacade responsibilityFacade) {
        this.myServer = server;
        this.projectManager = projectManager;
        this.responsibilityFacade = responsibilityFacade;
    }

    @Override
    public ModelAndView handleRequest(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) throws Exception {
        return null;
    }
}
