package com.hp.octane.plugins.jetbrains.teamcity.actions;

        import com.hp.octane.plugins.common.bridge.BridgesService;
        import com.hp.octane.plugins.common.configuration.ServerConfiguration;
        import com.hp.octane.plugins.jetbrains.teamcity.NGAPlugin;
        import com.hp.octane.plugins.jetbrains.teamcity.configuration.ConfigurationService;
        import com.hp.octane.plugins.jetbrains.teamcity.configuration.MqmProject;
        import com.hp.octane.plugins.jetbrains.teamcity.factories.ModelFactory;
import com.hp.octane.plugins.jetbrains.teamcity.utils.Config;
import com.hp.octane.plugins.jetbrains.teamcity.utils.ConfigManager;
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
 * Created by gadiel on 14/01/2016.
 */

public class AdminActionController extends AbstractActionController {
    @Autowired
    private static final Logger logger = Logger.getLogger(AdminActionController.class.getName());
    private SBuildServer m_server;
    PluginDescriptor m_descriptor;

    public AdminActionController(SBuildServer server, ProjectManager projectManager,
                                    BuildTypeResponsibilityFacade responsibilityFacade, ModelFactory modelFactory,ProjectSettingsManager projectSettingsManager, PluginDescriptor descriptor) {
        super(server,projectManager,responsibilityFacade,modelFactory);
        logger.info("AdminActionController");
        m_server = server;
        m_descriptor = descriptor;
    }

    @Override
    public ModelAndView handleRequest(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) throws Exception {

        PrintWriter writer = httpServletResponse.getWriter();

        try {
            String username = httpServletRequest.getParameter("username1");
            String password = httpServletRequest.getParameter("password1");
            String url_str = httpServletRequest.getParameter("server");

            MqmProject mqmProject = new MqmProject("","");
            try {
                mqmProject = ConfigurationService.parseUiLocation(url_str);
            } catch (Exception e) {
                e.printStackTrace();
            }

            Config cfg = NGAPlugin.getInstance().getConfig();
            ConfigManager cfgManager = ConfigManager.getInstance(m_descriptor, m_server);

            // updating the cfg file parameters
            cfg.setUsername(username);
            cfg.setSecretPassword(password);
            cfg.setUiLocation(url_str);
            cfg.setSharedSpace(mqmProject.getSharedSpace());
            cfg.setLocation(mqmProject.getLocation());
            cfgManager.jaxbObjectToXML(cfg);        // save the new parameters at the config file

            ServerConfiguration serverConfiguration  = new ServerConfiguration(
                    mqmProject.getLocation(),
                    mqmProject.getSharedSpace(),
                    username,
                    password,
                    "");

            BridgesService.getInstance().updateBridge(serverConfiguration);

            writer.write("Updated successfully");   // add cfgManager.printConfig() here to print the config details
        }
        catch(Exception e)
        {
            writer.write(e.toString());

        }
        return null;
    }

    @Override
    protected Object buildResults(HttpServletRequest request, HttpServletResponse response){
        return null;
    }


}
