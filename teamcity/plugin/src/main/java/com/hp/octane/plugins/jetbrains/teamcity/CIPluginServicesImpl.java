package com.hp.octane.plugins.jetbrains.teamcity;

import com.hp.nga.integrations.api.CIPluginServices;
import com.hp.nga.integrations.dto.DTOFactory;
import com.hp.nga.integrations.dto.configuration.CIProxyConfiguration;
import com.hp.nga.integrations.dto.configuration.NGAConfiguration;
import com.hp.nga.integrations.dto.general.CIJobsList;
import com.hp.nga.integrations.dto.general.CIPluginInfo;
import com.hp.nga.integrations.dto.general.CIServerInfo;
import com.hp.nga.integrations.dto.general.CIServerTypes;
import com.hp.nga.integrations.dto.pipelines.BuildHistory;
import com.hp.nga.integrations.dto.pipelines.PipelineNode;
import com.hp.nga.integrations.dto.snapshots.SnapshotNode;
import com.hp.octane.plugins.jetbrains.teamcity.configuration.NGAConfig;
import com.hp.octane.plugins.jetbrains.teamcity.factories.ModelFactory;

import java.io.File;
import java.util.logging.Logger;

/**
 * Created by gullery on 21/01/2016.
 * <p/>
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

		return DTOFactory.getInstance().newDTO(CIServerInfo.class)
				.setInstanceId(NGAPlugin.getInstance().getConfig().getIdentity())
				.setInstanceIdFrom(NGAPlugin.getInstance().getConfig().getIdentityFromAsLong())
				.setSendingTime(System.currentTimeMillis())
				.setType(CIServerTypes.TEAMCITY)
				.setUrl(serverUrl)
				.setVersion(pluginVersion);
	}

	@Override
	public CIPluginInfo getPluginInfo() {
		return DTOFactory.getInstance().newDTO(CIPluginInfo.class)
				.setVersion(pluginVersion);
	}

	@Override
	public File getAllowedNGAStorage() {
		return null;
	}

	@Override
	public NGAConfiguration getNGAConfiguration() {
		NGAConfig config = NGAPlugin.getInstance().getConfig();
		return DTOFactory.getInstance().newDTO(NGAConfiguration.class)
				.setUrl(config.getUiLocation())
				.setSharedSpace(config.getSharedSpace())
				.setApiKey(config.getUsername())
				.setSecret(config.getSecretPassword());
	}

	//  TODO: to be implemented
	@Override
	public CIProxyConfiguration getProxyConfiguration() {
		return null;
	}

	@Override
	public CIJobsList getJobsList(boolean includeParameters) {
		return ModelFactory.CreateProjectList();
	}

	@Override
	public PipelineNode getPipeline(String rootCIJobId) {
		return ModelFactory.createStructure(rootCIJobId);
	}

	//TODO: implement..
	@Override
	public void runPipeline(String ciJobId, String originalBody) {
		return;
	}

	@Override
	public SnapshotNode getSnapshotLatest(String ciJobId, boolean subTree) {
		return ModelFactory.createSnapshot(ciJobId);
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
