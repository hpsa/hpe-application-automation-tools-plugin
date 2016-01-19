package com.hp.octane.plugins.jetbrains.teamcity.actions;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hp.octane.plugins.jetbrains.teamcity.NGAPlugin;
import com.hp.octane.plugins.jetbrains.teamcity.factories.ModelFactory;
import com.hp.octane.plugins.jetbrains.teamcity.utils.Config;
import com.hp.octane.plugins.jetbrains.teamcity.utils.ConfigManager;
import jetbrains.buildServer.responsibility.BuildTypeResponsibilityFacade;
import jetbrains.buildServer.serverSide.ProjectManager;
import jetbrains.buildServer.serverSide.SBuildServer;
import jetbrains.buildServer.serverSide.ServerPaths;
import jetbrains.buildServer.serverSide.settings.ProjectSettingsManager;
import jetbrains.buildServer.web.openapi.PluginDescriptor;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.FileInputStream;
import java.io.IOException;
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
                                 BuildTypeResponsibilityFacade responsibilityFacade, ModelFactory modelFactory, ProjectSettingsManager projectSettingsManager, PluginDescriptor descriptor) {
        super(server,projectManager,responsibilityFacade,modelFactory);
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
            //  ConfigManager cfgManager =ConfigManager.getInstance(m_descriptor,m_server);
            //   Config cfg = cfgManager.jaxbXMLToObject();
            //   cfgManager.jaxbObjectToXML(cfg);

            String jsonInString = mapper.writeValueAsString(cfg);
            PrintWriter writer = httpServletResponse.getWriter();
            writer.write(jsonInString);

        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }



}
