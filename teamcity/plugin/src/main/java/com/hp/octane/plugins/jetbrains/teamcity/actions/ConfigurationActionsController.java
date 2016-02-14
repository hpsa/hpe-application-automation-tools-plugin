package com.hp.octane.plugins.jetbrains.teamcity.actions;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hp.octane.plugins.common.bridge.BridgesService;
import com.hp.octane.plugins.common.configuration.ServerConfiguration;
import com.hp.octane.plugins.jetbrains.teamcity.NGAPlugin;
import com.hp.octane.plugins.jetbrains.teamcity.configuration.ConfigurationService;
import com.hp.octane.plugins.jetbrains.teamcity.configuration.MqmProject;
import com.hp.octane.plugins.jetbrains.teamcity.utils.Config;
import com.hp.octane.plugins.jetbrains.teamcity.utils.ConfigManager;
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

/**
 * Created by lazara on 14/02/2016.
 */
public class ConfigurationActionsController extends AbstractActionController {

    private SBuildServer m_server;
    private PluginDescriptor m_descriptor;
    private static final String CLIENT_TYPE = ConfigurationService.CLIENT_TYPE;


    public ConfigurationActionsController(SBuildServer server, ProjectManager projectManager,
                                          BuildTypeResponsibilityFacade responsibilityFacade,
                                          ProjectSettingsManager projectSettingsManager, PluginDescriptor descriptor) {
        m_server = server;
        m_descriptor = descriptor;
    }


    @Override
    protected Object buildResults(HttpServletRequest request, HttpServletResponse response) {
        return null;
    }

    @Override
    public ModelAndView handleRequest(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) {

        String username = httpServletRequest.getParameter("username1");
        String password = httpServletRequest.getParameter("password1");
        String url_str = httpServletRequest.getParameter("server");


        MqmProject mqmProject = null;
        try {
            mqmProject = ConfigurationService.parseUiLocation(url_str);
        } catch (Exception e) {
            e.printStackTrace();
        }

        ServerConfiguration serverConfiguration  = new ServerConfiguration(
                mqmProject.getLocation(),
                mqmProject.getSharedSpace(),
                username,
                password,
                "");
        String returnStr = handleTestRequest(serverConfiguration);
        PrintWriter writer;

        try
        {
            writer = httpServletResponse.getWriter();
            writer.write(returnStr);
        }

        catch(IOException e){}
        return null;

    }

    public String testConfiguration(ServerConfiguration serverConf) {
        return ConfigurationService.checkConfiguration(serverConf.location,serverConf.sharedSpace,
                serverConf.username,serverConf.password,CLIENT_TYPE);
    }

    public String updateConfiguration(ServerConfiguration serverConf,String url) throws Exception {

        Config cfg = NGAPlugin.getInstance().getConfig();
        ConfigManager cfgManager = ConfigManager.getInstance(m_descriptor, m_server);

        // updating the cfg file parameters
        cfg.setUsername(serverConf.username);
        cfg.setSecretPassword(serverConf.password);
        cfg.setUiLocation(url);
        cfg.setSharedSpace(serverConf.sharedSpace);
        cfg.setLocation(serverConf.location);
        cfgManager.jaxbObjectToXML(cfg);        // save the new parameters at the config file

        BridgesService.getInstance().updateBridge(serverConf);

        return "Updated successfully";
    }

    public String reloadConfiguration()
    {
        try {
            ObjectMapper mapper = new ObjectMapper();
            NGAPlugin ngaPlugin = NGAPlugin.getInstance();
            Config cfg = ngaPlugin.getConfig();

            String jsonInString = null;
            jsonInString = mapper.writeValueAsString(cfg);
            return jsonInString;

        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        return null;

    }

}
