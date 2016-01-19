package com.hp.octane.plugins.jetbrains.teamcity.actions;

        import com.hp.octane.plugins.jetbrains.teamcity.NGAPlugin;
        import com.hp.octane.plugins.jetbrains.teamcity.factories.ModelFactory;
        import com.hp.octane.plugins.jetbrains.teamcity.model.pipeline.StructureItem;
        import com.hp.octane.plugins.jetbrains.teamcity.utils.Config;
        import com.hp.octane.plugins.jetbrains.teamcity.utils.ConfigManager;
        import com.hp.octane.plugins.jetbrains.teamcity.utils.Utils;
        import jetbrains.buildServer.responsibility.BuildTypeResponsibilityFacade;
        import jetbrains.buildServer.serverSide.ProjectManager;
        import jetbrains.buildServer.serverSide.SBuildServer;
        import jetbrains.buildServer.serverSide.ServerPaths;
        import jetbrains.buildServer.serverSide.settings.ProjectSettings;
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
        import java.io.*;
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
            int start = url_str.indexOf("?p=");
            int end = (url_str.substring(start)).indexOf("/");
            if(end!=-1) {
                System.out.println( "if");
                sharedSpace = url_str.substring(start + 3, start + end);
                uiLocation = url_str.substring(0,start+end);
            }
            else
            {
                System.out.println( "else");
                sharedSpace = url_str.substring(start + 3);
                uiLocation=url_str;
            }
            // updating the cfg file parameters
            cfg.setUsername(username);
            cfg.setSecretPassword(password);
            cfg.setUiLocation(uiLocation);
            cfg.setSharedSpace(sharedSpace);
            cfgManager.jaxbObjectToXML(cfg);        // save the new parameters at the config file
            writer.write("Updated successfully<br><br>"+cfgManager.printConfig());   // +cfgManager.printConfig() has to be removed. for debugging only
        }
        catch(Exception e)
        {
            writer.write(e.toString());

        }
        return null;
    }


}
