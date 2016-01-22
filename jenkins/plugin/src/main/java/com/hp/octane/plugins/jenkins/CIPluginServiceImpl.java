package com.hp.octane.plugins.jenkins;

import com.hp.nga.integrations.api.CIPluginService;
import com.hp.nga.integrations.configuration.NGAConfiguration;
import com.hp.nga.integrations.dto.builds.SnapshotDTO;
import com.hp.nga.integrations.dto.general.CIServerTypes;
import com.hp.nga.integrations.dto.general.PluginInfo;
import com.hp.nga.integrations.dto.general.ServerInfo;
import com.hp.nga.integrations.dto.parameters.ParameterType;
import com.hp.nga.integrations.dto.projects.ProjectsList;
import com.hp.octane.plugins.jenkins.OctanePlugin;
import com.hp.octane.plugins.jenkins.configuration.ServerConfiguration;
import com.hp.octane.plugins.jenkins.model.api.ParameterConfig;
import com.hp.octane.plugins.jenkins.model.processors.parameters.ParameterProcessors;
import hudson.model.AbstractProject;
import jenkins.model.Jenkins;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by gullery on 21/01/2016.
 * <p>
 * Jenkins CI Server oriented extension of CI Data Provider
 */

public class CIPluginServiceImpl implements CIPluginService {
	@Override
	public ServerInfo getServerInfo() {
		ServerInfo result = new ServerInfo();
		String serverUrl = Jenkins.getInstance().getRootUrl();
		if (serverUrl != null && serverUrl.endsWith("/")) {
			serverUrl = serverUrl.substring(0, serverUrl.length() - 1);
		}
		result.setType(CIServerTypes.JENKINS);
		result.setVersion(Jenkins.VERSION);
		result.setUrl(serverUrl);
		result.setInstanceId(Jenkins.getInstance().getPlugin(OctanePlugin.class).getIdentity());
		result.setInstanceIdFrom(Jenkins.getInstance().getPlugin(OctanePlugin.class).getIdentityFrom());
		result.setSendingTime(System.currentTimeMillis());
		return result;
	}

	@Override
	public PluginInfo getPluginInfo() {
		PluginInfo result = new PluginInfo();
		result.setVersion(Jenkins.getInstance().getPlugin(OctanePlugin.class).getWrapper().getVersion());
		return result;
	}

	@Override
	public NGAConfiguration getNGAConfiguration() {
		NGAConfiguration result = new NGAConfiguration();
		ServerConfiguration serverConfiguration = Jenkins.getInstance().getPlugin(OctanePlugin.class).getServerConfiguration();
		result.setUrl(serverConfiguration.location);
		result.setSharedSpace(Long.parseLong(serverConfiguration.sharedSpace));
		result.setUsername(serverConfiguration.username);
		result.setPassword(serverConfiguration.password);
		return result;
	}

	@Override
	public ProjectsList getProjectsList(boolean includeParameters) {
		ProjectsList result = new ProjectsList();
		ProjectsList.ProjectConfig tmpConfig;
		AbstractProject tmpProject;
		List<ProjectsList.ProjectConfig> list = new ArrayList<ProjectsList.ProjectConfig>();
		List<String> itemNames = (List<String>) Jenkins.getInstance().getTopLevelItemNames();
		for (String name : itemNames) {
			tmpProject = (AbstractProject) Jenkins.getInstance().getItem(name);
			tmpConfig = new ProjectsList.ProjectConfig();
			tmpConfig.setName(name);
			if (includeParameters) {
				ParameterConfig[] tmpList = ParameterProcessors.getConfigs(tmpProject);
				List<com.hp.nga.integrations.dto.parameters.ParameterConfig> configs = new ArrayList<com.hp.nga.integrations.dto.parameters.ParameterConfig>();
				for (ParameterConfig pc : tmpList) {
					configs.add(new com.hp.nga.integrations.dto.parameters.ParameterConfig(
							ParameterType.fromValue(pc.getType()),
							pc.getName(),
							pc.getDescription(),
							pc.getDefaultValue(),
							pc.getChoices() == null ? null : pc.getChoices().toArray(new Object[pc.getChoices().size()])
					));
				}
				tmpConfig.setParameters(configs.toArray(new com.hp.nga.integrations.dto.parameters.ParameterConfig[configs.size()]));
			}
			list.add(tmpConfig);
		}
		result.setJobs(list.toArray(new ProjectsList.ProjectConfig[list.size()]));
		return result;
	}

	@Override
	public SnapshotDTO getLatestSnapshot(String ciProjectId, String ciBuildId, boolean subTree) {
		return null;
	}
}
