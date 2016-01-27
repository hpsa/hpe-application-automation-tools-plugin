package com.hp.octane.plugins.jenkins;

import com.hp.nga.integrations.api.CIPluginService;
import com.hp.nga.integrations.configuration.NGAConfiguration;
import com.hp.nga.integrations.dto.general.CIServerTypes;
import com.hp.nga.integrations.dto.general.PluginInfoDTO;
import com.hp.nga.integrations.dto.general.ServerInfoDTO;
import com.hp.nga.integrations.dto.parameters.ParameterConfig;
import com.hp.nga.integrations.dto.pipelines.StructureItem;
import com.hp.nga.integrations.dto.projects.JobsListDTO;
import com.hp.nga.integrations.dto.snapshots.SnapshotItem;
import com.hp.octane.plugins.jenkins.configuration.ServerConfiguration;
import com.hp.octane.plugins.jenkins.model.ModelFactory;
import com.hp.octane.plugins.jenkins.model.processors.parameters.ParameterProcessors;
import hudson.model.AbstractProject;
import jenkins.model.Jenkins;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by gullery on 21/01/2016.
 * <p>
 * Jenkins CI Server oriented extension of CI Data Provider
 */

public class CIPluginServiceImpl implements CIPluginService {
	@Override
	public ServerInfoDTO getServerInfo() {
		ServerInfoDTO result = new ServerInfoDTO();
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
	public PluginInfoDTO getPluginInfo() {
		PluginInfoDTO result = new PluginInfoDTO();
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
	public JobsListDTO getProjectsList(boolean includeParameters) {

		JobsListDTO result = new JobsListDTO();
		JobsListDTO.ProjectConfig tmpConfig;
		AbstractProject tmpProject;
		List<JobsListDTO.ProjectConfig> list = new ArrayList<JobsListDTO.ProjectConfig>();
		List<String> itemNames = (List<String>) Jenkins.getInstance().getTopLevelItemNames();
		for (String name : itemNames) {
			tmpProject = (AbstractProject) Jenkins.getInstance().getItem(name);
			tmpConfig = new JobsListDTO.ProjectConfig();
			tmpConfig.setName(name);
			tmpConfig.setCiId(name);
			if (includeParameters) {
				List<ParameterConfig> tmpList = ParameterProcessors.getConfigs(tmpProject);
				List<com.hp.nga.integrations.dto.parameters.ParameterConfig> configs = new ArrayList<com.hp.nga.integrations.dto.parameters.ParameterConfig>();
				for (ParameterConfig pc : tmpList) {
					configs.add(new com.hp.nga.integrations.dto.parameters.ParameterConfig(
							pc.getType(),
							pc.getName(),
							pc.getDescription(),
							pc.getDefaultValue(),
							pc.getChoices() == null ? null : pc.getChoices()
					));
				}
				tmpConfig.setParameters(configs.toArray(new com.hp.nga.integrations.dto.parameters.ParameterConfig[configs.size()]));
			}
			list.add(tmpConfig);
		}
		result.setJobs(list.toArray(new JobsListDTO.ProjectConfig[list.size()]));
		return result;
	}

	@Override
	public StructureItem getPipeline(String rootCIJobId) {

		try {
			rootCIJobId = URLDecoder.decode(rootCIJobId, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		AbstractProject project = (AbstractProject) Jenkins.getInstance().getItem(rootCIJobId);
		return ModelFactory.createStructureItem(project);
	}

	@Override
	public SnapshotItem getSnapshotLatest(String ciJobId, String ciBuildId, boolean subTree) {
		return null;
	}
}
