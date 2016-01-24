package com.hp.octane.plugins.jetbrains.teamcity.actions;

        import com.hp.octane.plugins.common.bridge.BridgesService;
        import com.hp.octane.plugins.common.configuration.ServerConfiguration;
        import com.hp.octane.plugins.jetbrains.teamcity.NGAPlugin;
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
        NGAPlugin ngaPlugin = NGAPlugin.getInstance();
        Config cfg = ngaPlugin.getConfig();

        PrintWriter writer = httpServletResponse.getWriter();

        ConfigManager cfgManager = ConfigManager.getInstance(m_descriptor,m_server);

        try {
            String username = httpServletRequest.getParameter("username1");
            String password = httpServletRequest.getParameter("password1");
            String url_str = httpServletRequest.getParameter("server");

            // separating the sharedSpace from the uiLocation
            String sharedSpace;
            String uiLocation;
            int start = url_str.indexOf("p=");
            int end = (url_str.substring(start)).indexOf("/");
            if(end!=-1) {
                sharedSpace = url_str.substring(start + 2, start + end);
                uiLocation = url_str.substring(0,start+end);
            }
            else
            {
                sharedSpace = url_str.substring(start + 2);
                uiLocation=url_str;
            }

            int index=0;
            for(int i=0; i<url_str.length(); i++)
                if ((url_str.charAt(i))==':')
                    index = i;


            String Location = url_str.substring(0,index+5);


            // updating the cfg file parameters
            cfg.setUsername(username);
            cfg.setSecretPassword(password);
            cfg.setUiLocation(uiLocation);
            cfg.setSharedSpace(sharedSpace);
            cfg.setLocation(Location);
            cfgManager.jaxbObjectToXML(cfg);        // save the new parameters at the config file


            Config config = NGAPlugin.getInstance().getConfig();
            ServerConfiguration serverConfiguration  = new ServerConfiguration(
                    Location,
                    sharedSpace,
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
