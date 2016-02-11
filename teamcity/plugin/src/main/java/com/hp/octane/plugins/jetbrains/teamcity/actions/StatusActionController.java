package com.hp.octane.plugins.jetbrains.teamcity.actions;

import com.hp.nga.integrations.dto.DTOFactory;
import com.hp.nga.integrations.dto.general.CIServerTypes;
import com.hp.nga.integrations.dto.general.PluginInfo;
import com.hp.octane.plugins.jetbrains.teamcity.NGAPlugin;
import jetbrains.buildServer.responsibility.BuildTypeResponsibilityFacade;
import jetbrains.buildServer.serverSide.ProjectManager;
import jetbrains.buildServer.serverSide.SBuildServer;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Created by lazara on 27/12/2015.
 */
public class StatusActionController extends AbstractActionController {
//    private final SBuildServer myServer;
//    private final ProjectManager projectManager;
//    private final BuildTypeResponsibilityFacade responsibilityFacade;

	public StatusActionController(SBuildServer server, ProjectManager projectManager, BuildTypeResponsibilityFacade responsibilityFacade) {
//        this.myServer = server;
//        this.projectManager = projectManager;
//        this.responsibilityFacade = responsibilityFacade;
	}

	@Override
	protected Object buildResults(HttpServletRequest request, HttpServletResponse response) {

		String version = "9.1.5";
		String serverUrl = "http://localhost:8081";
		if (serverUrl != null && serverUrl.endsWith("/")) {
			serverUrl = serverUrl.substring(0, serverUrl.length() - 1);
		}

		com.hp.nga.integrations.dto.general.ServerInfo serverInfo = DTOFactory.instance.newDTO(com.hp.nga.integrations.dto.general.ServerInfo.class);
		serverInfo.setInstanceId(NGAPlugin.getInstance().getConfig().getIdentity());
		serverInfo.setInstanceIdFrom(NGAPlugin.getInstance().getConfig().getIdentityFromAsLong());
		serverInfo.setSendingTime(System.currentTimeMillis());
		serverInfo.setType(CIServerTypes.TEAMCITY);
		serverInfo.setUrl(serverUrl);
		serverInfo.setVersion(version);

		return serverInfo;
	}

	//TODO:Add to common lib
	public static final class ServerInfo {
		private static final CIServerTypes type = CIServerTypes.TEAMCITY;
		private static final String version = "9.1.5";
		private String url;

		private String instanceId = NGAPlugin.getInstance().getConfig().getIdentity();//Jenkins.getInstance().getPlugin(OctanePlugin.class).getIdentity();
		private Long instanceIdFrom = NGAPlugin.getInstance().getConfig().getIdentityFromAsLong();//Jenkins.getInstance().getPlugin(OctanePlugin.class).getIdentityFrom();
		private Long sendingTime;

		public ServerInfo() {
			String serverUrl = "http://localhost:8081";
			if (serverUrl != null && serverUrl.endsWith("/"))
				serverUrl = serverUrl.substring(0, serverUrl.length() - 1);
			this.url = serverUrl;
			this.sendingTime = System.currentTimeMillis();
		}


		public CIServerTypes getType() {
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
			return DTOFactory.instance.newDTO(PluginInfo.class);
		}
	}
}
