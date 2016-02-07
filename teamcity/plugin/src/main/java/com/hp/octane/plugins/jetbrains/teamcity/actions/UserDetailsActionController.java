package com.hp.octane.plugins.jetbrains.teamcity.actions;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hp.octane.plugins.jetbrains.teamcity.NGAPlugin;
import com.hp.octane.plugins.jetbrains.teamcity.utils.Config;
import jetbrains.buildServer.responsibility.BuildTypeResponsibilityFacade;
import jetbrains.buildServer.serverSide.ProjectManager;
import jetbrains.buildServer.serverSide.SBuildServer;
import jetbrains.buildServer.serverSide.settings.ProjectSettingsManager;
import jetbrains.buildServer.web.openapi.PluginDescriptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.PrintWriter;
import java.util.logging.Logger;

/**
 * Created by gadiel on 18/01/2016.
 */
public class UserDetailsActionController extends AbstractActionController{
    @Autowired
    private static final Logger logger = Logger.getLogger(AdminActionController.class.getName());
    private SBuildServer m_server;
    PluginDescriptor m_descriptor;

    Config m_config;
    public UserDetailsActionController(SBuildServer server, ProjectManager projectManager,
                                 BuildTypeResponsibilityFacade responsibilityFacade , ProjectSettingsManager projectSettingsManager, PluginDescriptor descriptor) {
        m_server = server;
        m_descriptor = descriptor;
    }

    @Override
    public ModelAndView handleRequest(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse)
    {
        try {
            ObjectMapper mapper = new ObjectMapper();
            NGAPlugin ngaPlugin = NGAPlugin.getInstance();
            Config cfg = ngaPlugin.getConfig();

            String jsonInString = mapper.writeValueAsString(cfg);
            PrintWriter writer = httpServletResponse.getWriter();
            writer.write(jsonInString);

        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    protected Object buildResults(HttpServletRequest request, HttpServletResponse response){
        return null;
    }


}
