package com.hp.octane.plugins.jetbrains.teamcity.actions;


import com.hp.octane.dto.projects.ProjectsList;
import com.hp.octane.plugins.jetbrains.teamcity.factories.ModelFactory;
import com.hp.octane.plugins.jetbrains.teamcity.utils.Utils;
import jetbrains.buildServer.responsibility.BuildTypeResponsibilityFacade;
import jetbrains.buildServer.serverSide.ProjectManager;
import jetbrains.buildServer.serverSide.SBuildServer;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


public class PluginActionsController extends AbstractActionController {

    public PluginActionsController(final SBuildServer server, ProjectManager manager,
                                   BuildTypeResponsibilityFacade descriptor, ModelFactory modelFactory) {
        super(server,manager,descriptor,modelFactory);
    }

    public ModelAndView handleRequest(HttpServletRequest request, HttpServletResponse response) throws Exception {

        //BuildConfigurationList
        ProjectsList projectsList = modelFactory.CreateProjectList();
        Utils.updateResponse(projectsList,request,response);
        return null;

    }
}