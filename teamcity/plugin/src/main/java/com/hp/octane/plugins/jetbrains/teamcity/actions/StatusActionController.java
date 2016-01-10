package com.hp.octane.plugins.jetbrains.teamcity.actions;

import com.hp.octane.dto.general.PluginInfo;
import com.hp.octane.plugins.jetbrains.teamcity.DummyPluginConfiguration;
import com.hp.octane.plugins.jetbrains.teamcity.utils.Utils;
import jetbrains.buildServer.responsibility.BuildTypeResponsibilityFacade;
import jetbrains.buildServer.serverSide.ProjectManager;
import jetbrains.buildServer.serverSide.SBuildServer;
import jetbrains.buildServer.serverSide.TeamCityProperties;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.Controller;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.PrintWriter;
import java.util.List;

/**
 * Created by lazara on 27/12/2015.
 */
public class StatusActionController implements Controller {
    private final SBuildServer myServer;
    private final ProjectManager projectManager;
    private final BuildTypeResponsibilityFacade responsibilityFacade;

    public StatusActionController(SBuildServer server, ProjectManager projectManager, BuildTypeResponsibilityFacade responsibilityFacade) {
        this.myServer = server;
        this.projectManager = projectManager;
        this.responsibilityFacade = responsibilityFacade;
    }

    @Override
    public ModelAndView handleRequest(HttpServletRequest request, HttpServletResponse response) throws Exception {
        Utils.updateResponse(new PluginStatus(), request, response);
        return null;
    }

    //TODO:Add to common lib
    public static final class ServerInfo {
        private static final String type = "teamcity";
        private static final String version = "9.1.5";
        private String url;
        private String instanceId = DummyPluginConfiguration.identity;//Jenkins.getInstance().getPlugin(OctanePlugin.class).getIdentity();
        private Long instanceIdFrom = DummyPluginConfiguration.identityFrom;//Jenkins.getInstance().getPlugin(OctanePlugin.class).getIdentityFrom();
        private Long sendingTime;

        public ServerInfo() {
            String serverUrl = "http://teamcity:8888/httpAuth";//Jenkins.getInstance().getRootUrl();
            if (serverUrl != null && serverUrl.endsWith("/"))
                serverUrl = serverUrl.substring(0, serverUrl.length() - 1);
            this.url = serverUrl;
            this.sendingTime = System.currentTimeMillis();
        }


        public String getType() {
            return type;
        }

        public String getVersion() {
            return version;
        }

        public String getUrl() {
            return url;
        }

        public String getInstanceId() {
            return instanceId;
        }

        public Long getInstanceIdFrom() {
            return instanceIdFrom;
        }

        public Long getSendingTime() {
            return sendingTime;
        }
    }
    public static final class PluginStatus {
        public ServerInfo getServer() {
            return new ServerInfo();
        }

        public PluginInfo getPlugin() {
            return new PluginInfo();
        }

//        public List<EventsClient> getEventsClients() {
//            return EventsService.getExtensionInstance().getStatus();
//        }
    }
}
