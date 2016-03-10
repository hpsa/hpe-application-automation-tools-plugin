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
import jetbrains.buildServer.serverSide.SBuildServer;
import jetbrains.buildServer.web.openapi.PluginDescriptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.Controller;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by lazara on 14/02/2016.
 */
public class ConfigurationActionsController implements Controller {

    @Autowired
    private static final Logger logger = Logger.getLogger(ConfigurationActionsController.class.getName());
    private SBuildServer m_server;
    private PluginDescriptor m_descriptor;
    private static final String CLIENT_TYPE = ConfigurationService.CLIENT_TYPE;



    public ConfigurationActionsController(SBuildServer server, PluginDescriptor descriptor) {
        m_server = server;
        m_descriptor = descriptor;
    }

    @Override
    public ModelAndView handleRequest(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) {

        String returnStr="";
        String action = httpServletRequest.getParameter("action");

        if (action==null || action.equals("")) {
            returnStr = reloadConfiguration();
        }else{

            String username = httpServletRequest.getParameter("username1");
            String password = httpServletRequest.getParameter("password1");
            String url_str = httpServletRequest.getParameter("server");


            MqmProject mqmProject;
            try {
                mqmProject = ConfigurationService.parseUiLocation(url_str);

                ServerConfiguration serverConfiguration  = new ServerConfiguration(
                        mqmProject.getLocation(),
                        mqmProject.getSharedSpace(),
                        username,
                        password,
                        "");

                if(action.equals("test")){
                    returnStr = testConfiguration(serverConfiguration);
                }else if(action.equals("save")){
                    returnStr = updateConfiguration(serverConfiguration,mqmProject);
                }

            } catch (Exception e) {
                returnStr = e.getMessage();
                e.printStackTrace();
            }

        }

        PrintWriter writer;
        try
        {
            writer = httpServletResponse.getWriter();
            writer.write(returnStr);
        }

        catch(IOException e){
            logger.log(Level.WARNING, e.getMessage());
        }
        return null;

    }

    public String testConfiguration(ServerConfiguration serverConf) {
        return ConfigurationService.checkConfiguration(serverConf.location,serverConf.sharedSpace,
                serverConf.username,serverConf.password,CLIENT_TYPE);
    }

    public String updateConfiguration(ServerConfiguration serverConf,MqmProject mqmProject){

        Config cfg = NGAPlugin.getInstance().getConfig();
        ConfigManager cfgManager = ConfigManager.getInstance(m_descriptor, m_server);

        // updating the cfg file parameters
        cfg.setUsername(serverConf.username);
        cfg.setSecretPassword(serverConf.password);
        cfg.setUiLocation(mqmProject.getLocation());
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
            logger.log(Level.WARNING, "failed to reload configuration: "+ e.getMessage());
            e.printStackTrace();
        }
        return null;

    }

}
