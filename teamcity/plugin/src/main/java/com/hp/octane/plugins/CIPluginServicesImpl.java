package com.hp.octane.plugins;

import com.hp.nga.integrations.api.CIPluginServices;
import com.hp.nga.integrations.dto.DTOFactory;
import com.hp.nga.integrations.dto.configuration.CIProxyConfiguration;
import com.hp.nga.integrations.dto.general.CIServerTypes;
import com.hp.nga.integrations.dto.general.CIPluginInfo;
import com.hp.nga.integrations.dto.general.CIServerInfo;
import com.hp.nga.integrations.dto.pipelines.BuildHistory;
import com.hp.nga.integrations.dto.pipelines.PipelineNode;
import com.hp.nga.integrations.dto.general.CIJobsList;
import com.hp.nga.integrations.dto.snapshots.SnapshotNode;
import com.hp.nga.integrations.dto.configuration.NGAConfiguration;
import com.hp.octane.plugins.jetbrains.teamcity.NGAPlugin;
import com.hp.octane.plugins.jetbrains.teamcity.factories.ModelFactory;
import com.hp.octane.plugins.jetbrains.teamcity.utils.Config;

import java.io.File;
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
	public CIServerInfo getServerInfo() {
		String serverUrl = "http://localhost:8081";
		if (serverUrl != null && serverUrl.endsWith("/")) {
			serverUrl = serverUrl.substring(0, serverUrl.length() - 1);
		}

		CIServerInfo CIServerInfo = DTOFactory.getInstance().newDTO(CIServerInfo.class);
		CIServerInfo.setInstanceId(NGAPlugin.getInstance().getConfig().getIdentity())
				.setInstanceIdFrom(NGAPlugin.getInstance().getConfig().getIdentityFromAsLong())
				.setSendingTime(System.currentTimeMillis())
				.setType(CIServerTypes.TEAMCITY)
				.setUrl(serverUrl)
				.setVersion(pluginVersion);

		return CIServerInfo;
	}

	@Override
	public CIPluginInfo getPluginInfo() {
		CIPluginInfo CIPluginInfo = DTOFactory.getInstance().newDTO(CIPluginInfo.class);
		CIPluginInfo.setVersion(pluginVersion);
		return CIPluginInfo;
	}

	@Override
	public File getAllowedNGAStorage() {
		//  not yet implemented, null means no storage available
		return null;
	}

	@Override
	//TODO: implement..
	public NGAConfiguration getNGAConfiguration() {
		Config config = NGAPlugin.getInstance().getConfig();
		NGAConfiguration ngaConfiguration = DTOFactory.getInstance().newDTO(NGAConfiguration.class);
		ngaConfiguration.setUrl(config.getUiLocation());
		ngaConfiguration.setClientId(config.getUsername());
		ngaConfiguration.setApiKey(config.getSecretPassword());
		if (config.getSharedSpace() != null && !config.getSharedSpace().isEmpty()) {
			ngaConfiguration.setSharedSpace(Long.parseLong(config.getSharedSpace()));
		}
		return ngaConfiguration;
	}

	@Override
	public CIProxyConfiguration getProxyConfiguration() {
		//  TODO: to be implemented
		return null;
	}

	@Override
	public CIJobsList getJobsList(boolean includeParameters) {

		CIJobsList CIJobsList = ModelFactory.CreateProjectList();
		return CIJobsList;

	}

	@Override
	public PipelineNode getPipeline(String rootCIJobId) {
		PipelineNode pipelineNode = ModelFactory.createStructure(rootCIJobId);
		return pipelineNode;
	}

	//TODO: implement..
	@Override
	public int runPipeline(String ciJobId, String originalBody) {
		return 404;
	}

	@Override
	public SnapshotNode getSnapshotLatest(String ciJobId, boolean subTree) {
		SnapshotNode snapshotNode = ModelFactory.createSnapshot(ciJobId);
		return snapshotNode;
	}

	//  TODO: implement
	@Override
	public SnapshotNode getSnapshotByNumber(String ciJobId, Integer ciBuildNumber, boolean subTree) {
		return null;
	}

	//TODO: implement: fill build history
	@Override
	public BuildHistory getHistoryPipeline(String ciJobId, String originalBody) {
		return DTOFactory.getInstance().newDTO(BuildHistory.class);
	}
}
