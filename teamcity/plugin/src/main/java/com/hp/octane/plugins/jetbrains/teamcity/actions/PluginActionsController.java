package com.hp.octane.plugins.jetbrains.teamcity.actions;


import com.hp.octane.plugins.jetbrains.teamcity.factories.ModelFactory;
import com.hp.octane.plugins.jetbrains.teamcity.factories.TeamCityModelFactory;
import com.hp.octane.plugins.jetbrains.teamcity.model.api.ProjectsList;
import com.hp.octane.plugins.jetbrains.teamcity.utils.Utils;
import jetbrains.buildServer.responsibility.BuildTypeResponsibilityFacade;
import jetbrains.buildServer.serverSide.ProjectManager;
import jetbrains.buildServer.serverSide.SBuildServer;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.Controller;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.PrintWriter;


public class PluginActionsController implements Controller {
    private final SBuildServer myServer;
    private final ProjectManager projectManager;
    private final BuildTypeResponsibilityFacade responsibilityFacade;
    private ModelFactory modelFactory;

    public PluginActionsController(final SBuildServer server, ProjectManager manager, BuildTypeResponsibilityFacade descriptor) {
        //super(server);
        this.myServer = server;
        this.projectManager = manager;
        this.responsibilityFacade = descriptor;
        modelFactory = TeamCityModelFactory.getInstance();
    }

    public ModelAndView handleRequest(HttpServletRequest request, HttpServletResponse response) throws Exception {

        //BuildConfigurationList
        ProjectsList projectsList = modelFactory.CreateProjectList(this.projectManager);
        updateResponse(projectsList,request,response);
        return null;

    }


    private void updateResponse( Object state, HttpServletRequest request, HttpServletResponse response) throws Exception {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        StringBuilder data = new StringBuilder();

        //BuildConfigurationHolder state = (BuildConfigurationHolder)map.get("ViewState");

        if (state != null) {
            data.append(Utils.jacksonRendering(state));
        }

        String[] jsonp = request.getParameterValues("jsonp");

        if (jsonp != null) {
            data.insert(0, jsonp[0] + "(");
            data.append(")\n");
        } else {
            data.append("\n");
        }
        PrintWriter writer = response.getWriter();
        writer.write(data.toString());
    }
}