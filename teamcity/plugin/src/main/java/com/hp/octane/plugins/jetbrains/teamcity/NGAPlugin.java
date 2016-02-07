package com.hp.octane.plugins.jetbrains.teamcity;

/**
 * Created by lazara on 23/12/2015.
 */

import com.hp.nga.integrations.services.SDKFactory;
import com.hp.nga.integrations.services.bridge.BridgeService;
import com.hp.octane.plugins.CIPluginServicesImpl;
import com.hp.octane.plugins.common.configuration.ServerConfiguration;
import com.hp.octane.plugins.jetbrains.teamcity.actions.*;
import com.hp.octane.plugins.jetbrains.teamcity.utils.Config;
import com.hp.octane.plugins.jetbrains.teamcity.utils.ConfigManager;
import jetbrains.buildServer.responsibility.BuildTypeResponsibilityFacade;
import jetbrains.buildServer.serverSide.ProjectManager;
import jetbrains.buildServer.serverSide.SBuildServer;
import jetbrains.buildServer.serverSide.ServerExtension;
import jetbrains.buildServer.serverSide.settings.ProjectSettingsManager;
import jetbrains.buildServer.web.openapi.PluginDescriptor;
import jetbrains.buildServer.web.openapi.WebControllerManager;

import java.util.Date;
import java.util.UUID;
import java.util.logging.Logger;

//import com.hp.octane.plugins.common.bridge.BridgesService;

//import com.hp.octane.plugins.common.bridge.BridgesService;

public class NGAPlugin implements ServerExtension {
    public static final String PLUGIN_NAME = NGAPlugin.class.getSimpleName().toLowerCase();
    private static final Logger logger = Logger.getLogger(NGAPlugin.class.getName());
    private final CIPluginServicesImpl ciPluginService;

    private String identity;
    private Long identityFrom;

    // inferred from uiLocation
    private final String PLUGIN_TYPE = "HPE_TEAMCITY_PLUGIN";

    private SBuildServer sBuildServer;
    private ProjectManager projectManager;
    private BuildTypeResponsibilityFacade responsibilityFacade;


    private static NGAPlugin plugin;
    private PluginDescriptor descriptor;



    private Config config;
    private ConfigManager configManager;
    public static String getPluginName() {
        return PLUGIN_NAME;
    }


    public NGAPlugin(SBuildServer sBuildServer,
                     ProjectManager projectManager,
                     BuildTypeResponsibilityFacade responsibilityFacade,
                     WebControllerManager webControllerManager,
                     ProjectSettingsManager projectSettingsManager,
                     PluginDescriptor pluginDescriptor) {
        logger.info("Init HPE MQM CI Plugin");
        sBuildServer.registerExtension(ServerExtension.class, PLUGIN_NAME, this);
        this.plugin = this;
        descriptor = pluginDescriptor;
        this.sBuildServer = sBuildServer;
        this.projectManager = projectManager;
        this.responsibilityFacade = responsibilityFacade;
//        server.addListener(new BuildEventListener());
        registerControllers(webControllerManager, projectManager, sBuildServer, projectSettingsManager, pluginDescriptor);
        configManager= ConfigManager.getInstance(descriptor, sBuildServer);
        config = configManager.jaxbXMLToObject();
        this.ciPluginService = new CIPluginServicesImpl();

        //  X Plugin will decide what's its pattern to provide instance/s of the implementation
        SDKFactory.init(ciPluginService);
        //  X Plugin will consume SDK's services elsewhere in the following manner
        //  EventsService eventsService = SDKFactory.getEventsService();

        //  These ones, once will become part of the SDK, will be hidden from X Plugin and initialized in SDK internally
//        EventsService.getExtensionInstance().updateClient(getServerConfiguration());
//        BridgesService.getExtensionInstance().updateBridge(getServerConfiguration());

        initOPB();
    }

    public SBuildServer getsBuildServer() {
        return sBuildServer;
    }


    public ProjectManager getProjectManager() {
        return projectManager;
    }

    public BuildTypeResponsibilityFacade getResponsibilityFacade() {
        return responsibilityFacade;
    }

    public static NGAPlugin getInstance() {
        return plugin;
    }

    private void registerControllers(WebControllerManager webControllerManager, ProjectManager projectManager, SBuildServer sBuildServer, ProjectSettingsManager projectSettingsManager, PluginDescriptor pluginDescriptor) {

        webControllerManager.registerController("/octane/jobs/**",
                new PluginActionsController(sBuildServer, projectManager, responsibilityFacade));

        webControllerManager.registerController("/octane/snapshot/**",
                new BuildActionsController(sBuildServer, projectManager, responsibilityFacade));

        webControllerManager.registerController("/octane/structure/**",
                new ProjectActionsController(sBuildServer, projectManager, responsibilityFacade));
        webControllerManager.registerController("/octane/status/**",
                new StatusActionController(sBuildServer, projectManager, responsibilityFacade));

        webControllerManager.registerController("/octane/admin/**",
                new AdminActionController(sBuildServer, projectManager, responsibilityFacade, projectSettingsManager, pluginDescriptor));

        webControllerManager.registerController("/octane/userDetails/**",
                new UserDetailsActionController(sBuildServer, projectManager, responsibilityFacade, projectSettingsManager, pluginDescriptor));

        webControllerManager.registerController("/octane/testConnection/**",
                new TestConnectionActionController(sBuildServer, projectManager, responsibilityFacade, projectSettingsManager, pluginDescriptor));

    }

    private void initOPB() {

        String Identity = config.getIdentity();
        if(Identity.equals("") || Identity.equals(null))
        {
            String newidentity = UUID.randomUUID().toString();      //creating the new parameters
            String newidentityFrom = String.valueOf(new Date().getTime());
            config.setIdentity(newidentity);                    // canging the parsms at the config object
            config.setIdentityFrom(newidentityFrom);
            configManager.jaxbObjectToXML(config);              // update the XML file
        }

        BridgeService.getInstance().updateBridge(ciPluginService.getNGAConfiguration());
//        BridgesService.getInstance().setCIType(PLUGIN_TYPE);
 //       BridgesService.getInstance().updateBridge(getServerConfiguration());
    }



    public Config getConfig() {
        return config;
    }


    public ServerConfiguration getServerConfiguration() {

        return new ServerConfiguration(
                config.getLocation(),
                config.getSharedSpace(),
                config.getUsername(),
                config.getSecretPassword(),
                ""


        );
    }

}
