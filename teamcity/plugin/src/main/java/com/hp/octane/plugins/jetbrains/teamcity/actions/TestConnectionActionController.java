package com.hp.octane.plugins.jetbrains.teamcity.actions;

import com.hp.octane.plugins.jetbrains.teamcity.configuration.ConfigurationService;
import com.hp.octane.plugins.jetbrains.teamcity.configuration.MqmProject;
import com.hp.octane.plugins.jetbrains.teamcity.factories.ModelFactory;
import jetbrains.buildServer.responsibility.BuildTypeResponsibilityFacade;
import jetbrains.buildServer.serverSide.ProjectManager;
import jetbrains.buildServer.serverSide.SBuildServer;
import jetbrains.buildServer.serverSide.settings.ProjectSettingsManager;
import jetbrains.buildServer.web.openapi.PluginDescriptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.logging.Logger;


/**
 * Created by gadiel on 21/01/2016.
 */
public class TestConnectionActionController extends AbstractActionController {
    private static final Logger logger = Logger.getLogger(TestConnectionActionController.class.getName());
    private static final String CLIENT_TYPE = ConfigurationService.CLIENT_TYPE;

    PluginDescriptor m_descriptor;

    public TestConnectionActionController(SBuildServer server, ProjectManager projectManager,
                                          BuildTypeResponsibilityFacade responsibilityFacade, ModelFactory modelFactory, ProjectSettingsManager projectSettingsManager, PluginDescriptor descriptor) {
        super(server, projectManager, responsibilityFacade, modelFactory);
        m_descriptor = descriptor;
    }

    @Override
    public ModelAndView handleRequest(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) {
       String username = httpServletRequest.getParameter("username1");
        String password = httpServletRequest.getParameter("password1");
        String url_str = httpServletRequest.getParameter("server");


        PrintWriter writer;
        String returnString="";
        try {
            MqmProject mqmProject = ConfigurationService.parseUiLocation(url_str);
            returnString= ConfigurationService.checkConfiguration(mqmProject.getLocation(), mqmProject.getSharedSpace(),
                    username, password,CLIENT_TYPE);


        } catch (Exception e) {
            e.printStackTrace();
            returnString = e.getMessage();
        }

        try
        {
            writer = httpServletResponse.getWriter();
            writer.write(returnString);
        }

        catch(IOException e){

        }

        return null;
    }

    @Override
    protected Object buildResults(HttpServletRequest request, HttpServletResponse response) {
        return null;
    }

}
