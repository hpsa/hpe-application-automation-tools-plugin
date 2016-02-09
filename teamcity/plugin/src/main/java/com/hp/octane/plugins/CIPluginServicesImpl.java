package com.hp.octane.plugins;

import com.hp.nga.integrations.api.CIPluginServices;
import com.hp.nga.integrations.dto.DTOFactory;
import com.hp.nga.integrations.dto.general.CIServerTypes;
import com.hp.nga.integrations.dto.general.PluginInfo;
import com.hp.nga.integrations.dto.general.ServerInfo;
import com.hp.nga.integrations.dto.general.PluginInfoImpl;
import com.hp.nga.integrations.dto.pipelines.BuildHistory;
import com.hp.nga.integrations.dto.pipelines.StructureItem;
import com.hp.nga.integrations.dto.general.JobsList;
import com.hp.nga.integrations.dto.snapshots.SnapshotItem;
import com.hp.nga.integrations.dto.configuration.NGAConfiguration;
import com.hp.octane.plugins.jetbrains.teamcity.NGAPlugin;
import com.hp.octane.plugins.jetbrains.teamcity.factories.ModelFactory;
import com.hp.octane.plugins.jetbrains.teamcity.utils.Config;

import java.util.logging.Logger;

/**
 * Created by gullery on 21/01/2016.
 * <p>
 * Jenkins CI Server oriented extension of CI Data Provider
 */

public class CIPluginServicesImpl implements CIPluginServices {
	private static final Logger logger = Logger.getLogger(CIPluginServicesImpl.class.getName());
	private static final String pluginVersion = "9.1.5";

	@Override
	public ServerInfo getServerInfo() {
		String serverUrl = "http://localhost:8081";
		if (serverUrl != null && serverUrl.endsWith("/")) {
			serverUrl = serverUrl.substring(0, serverUrl.length() - 1);
		}

		ServerInfo serverInfo = DTOFactory.createDTO(ServerInfo.class);
		serverInfo.setInstanceId(NGAPlugin.getInstance().getConfig().getIdentity())
				.setInstanceIdFrom(NGAPlugin.getInstance().getConfig().getIdentityFromAsLong())
				.setSendingTime(System.currentTimeMillis())
				.setType(CIServerTypes.TEAMCITY)
				.setUrl(serverUrl)
				.setVersion(pluginVersion);

		return serverInfo;
	}

	@Override
	public PluginInfo getPluginInfo() {
		PluginInfo pluginInfo = DTOFactory.createDTO(PluginInfoImpl.class);
		pluginInfo.setVersion(pluginVersion);
		return pluginInfo;
	}

	@Override
	//TODO: implement..
	public NGAConfiguration getNGAConfiguration() {
		Config config = NGAPlugin.getInstance().getConfig();
		NGAConfiguration ngaConfiguration = DTOFactory.createDTO(NGAConfiguration.class);
		ngaConfiguration.setUrl(config.getUiLocation());
		ngaConfiguration.setClientId(config.getUsername());
		ngaConfiguration.setApiKey(config.getSecretPassword());
		ngaConfiguration.setSharedSpace(Long.parseLong(config.getSharedSpace()));
		return ngaConfiguration;
	}

	@Override
	public JobsList getJobsList(boolean includeParameters) {

		JobsList jobsList = ModelFactory.CreateProjectList();
		return jobsList;

	}

	@Override
	public StructureItem getPipeline(String rootCIJobId) {
		StructureItem structureItem = ModelFactory.createStructure(rootCIJobId);
		return structureItem;
	}

	@Override
	//TODO: implement..
	public int runPipeline(String ciJobId, String originalBody) {
		return 404;
	}

	@Override
	public SnapshotItem getSnapshotLatest(String ciJobId, boolean subTree) {
		SnapshotItem snapshotItem = ModelFactory.createSnapshot(ciJobId);
		return snapshotItem;
	}

	@Override
	//TODO: implement: feel build history
	public BuildHistory getHistoryPipeline(String ciJobId, String originalBody) {
		return DTOFactory.createDTO(BuildHistory.class);
	}
}
