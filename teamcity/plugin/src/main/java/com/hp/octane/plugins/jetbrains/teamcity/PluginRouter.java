package com.hp.octane.plugins.jetbrains.teamcity;

/**
 * Created by lazara on 23/12/2015.
 */

import com.hp.octane.plugins.common.bridge.BridgesService;
import com.hp.octane.plugins.common.configuration.ServerConfiguration;
import com.hp.octane.plugins.jetbrains.teamcity.actions.BuildActionsController;
import com.hp.octane.plugins.jetbrains.teamcity.actions.PluginActionsController;
import com.hp.octane.plugins.jetbrains.teamcity.actions.ProjectActionsController;
import com.hp.octane.plugins.jetbrains.teamcity.client.TeamCityMqmRestClientFactory;
import jetbrains.buildServer.responsibility.BuildTypeResponsibilityFacade;
import jetbrains.buildServer.serverSide.ProjectManager;
import jetbrains.buildServer.serverSide.SBuildServer;
import jetbrains.buildServer.serverSide.ServerExtension;
import jetbrains.buildServer.web.openapi.WebControllerManager;
import org.apache.commons.lang.StringUtils;

import java.util.Date;
import java.util.UUID;
import java.util.logging.Logger;

public class PluginRouter implements ServerExtension {
    public static final String PLUGIN_NAME = PluginRouter.class.getSimpleName().toLowerCase();
    private static final Logger logger = Logger.getLogger(PluginRouter.class.getName());
    private String identity = "49849531-8f20-4d94-b49f-3f6864c97701";
    private Long identityFrom;
    private String uiLocation = "http://localhost:8080/ui?p=1001";
    private String username;
    private String password;
    private String impersonatedUser;

    // inferred from uiLocation
    private String location="http://localhost:8080";
    private String sharedSpace;

    public PluginRouter(SBuildServer server,
                        ProjectManager projectManager,
                        BuildTypeResponsibilityFacade responsibilityFacade,
                        WebControllerManager webControllerManager) {
        logger.info("Init HPE MQM CI Plugin");
        server.registerExtension(ServerExtension.class, PLUGIN_NAME, this);


        webControllerManager.registerController("/octane/jobs/**",
                new PluginActionsController(server, projectManager, responsibilityFacade));

        webControllerManager.registerController("/octane/snapshot/**",
                new BuildActionsController(server, projectManager, responsibilityFacade));

        webControllerManager.registerController("/octane/structure/**",
                new ProjectActionsController(server, projectManager, responsibilityFacade));
        initiPlugin();
        BridgesService.getInstance().setMqmRestClientFactory(new TeamCityMqmRestClientFactory());
        BridgesService.getInstance().updateBridge(getServerConfiguration());
    }

    private void initiPlugin() {
        if (StringUtils.isEmpty(identity)) {
            setIdentity(UUID.randomUUID().toString());
        }
        if (identityFrom == null || identityFrom == 0) {
            this.identityFrom = new Date().getTime();
        }
    }

    public ServerConfiguration getServerConfiguration() {
        return new ServerConfiguration(
                getLocation(),
                getSharedSpace(),
                getUsername(),
                getPassword(),
                getImpersonatedUser());
    }


    public String getIdentity() {
        return identity;
    }

    public String getUsername() {
        //return username;
        return "test@hp.com";
    }

    public String getPassword() {
        //return password;
        return "";
    }

    public String getLocation() {
        //return location;
        return "http://localhost.emea.hpqcorp.net:8080";
    }

    public String getSharedSpace() {
        //return sharedSpace;
        return "2001";
    }

    public String getImpersonatedUser() {
        return impersonatedUser;
    }

    public void setIdentity(String identity) {
        this.identity = identity;
    }
}
