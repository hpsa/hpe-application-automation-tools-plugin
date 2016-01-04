package com.hp.octane.plugins.jetbrains.teamcity.actions;

import jetbrains.buildServer.responsibility.BuildTypeResponsibilityFacade;
import jetbrains.buildServer.serverSide.ProjectManager;
import jetbrains.buildServer.serverSide.SBuildServer;
import jetbrains.buildServer.serverSide.SBuildType;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.Controller;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * Created by lazara on 27/12/2015.
 */
public class ProjectActionsController implements Controller {

    private final SBuildServer myServer;
    private final ProjectManager projectManager;
    private final BuildTypeResponsibilityFacade responsibilityFacade;

    public ProjectActionsController(SBuildServer server, ProjectManager projectManager, BuildTypeResponsibilityFacade responsibilityFacade) {
        this.myServer = server;
        this.projectManager = projectManager;
        this.responsibilityFacade = responsibilityFacade;
    }

    @Override
    public ModelAndView handleRequest(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) throws Exception {

        String buildConfigurationId = httpServletRequest.getParameter("id");
        SBuildType root = projectManager.findBuildTypeByExternalId("PrivatCloud_CheckDependencies2_A");

        List<SBuildType> dependencies = root.getDependencyReferences();

        //1.create items from his dependenices.
        //2. add himself to this dependencies
        //3. for each dependencies:
        return null;
    }

}
