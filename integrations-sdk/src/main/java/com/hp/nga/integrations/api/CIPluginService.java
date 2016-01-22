package com.hp.nga.integrations.api;

import com.hp.nga.integrations.configuration.NGAConfiguration;
import com.hp.nga.integrations.dto.builds.SnapshotDTO;
import com.hp.nga.integrations.dto.general.PluginInfo;
import com.hp.nga.integrations.dto.general.ServerInfo;
import com.hp.nga.integrations.dto.projects.ProjectsList;

/**
 * Created by gullery on 20/01/2016.
 * <p>
 * Composite API of all the endpoints to be implemented by a hosting CI Plugin for NGA use cases
 */

public interface CIPluginService {

	/**
	 * Provides CI Server information
	 *
	 * @return ServerInfo object; MUST NOT return null
	 */
	ServerInfo getServerInfo();

	/**
	 * Provides Plugin's information
	 *
	 * @return PluginInfo object; MUST NOT return null
	 */
	PluginInfo getPluginInfo();

	/**
	 * Provides NGA Server configuration (managed by plugin implementation)
	 *
	 * @return NGAConfiguration object; MUST NOT return null
	 */
	NGAConfiguration getNGAConfiguration();

	/**
	 * Provides a list of Projects existing on this CI Server
	 *
	 * @param includeParameters
	 * @return ProjectList object holding the list of the projects
	 */
	ProjectsList getProjectsList(boolean includeParameters);

	//  TODO: project's structure
	//  TODO: project's run
	//  TODO: Almog's history API (TBR)

	/**
	 * Provisioning of Snapshot of the specified CI Build
	 *
	 * @param ciProjectId
	 * @param ciBuildId
	 * @param subTree
	 * @return
	 */
	SnapshotDTO getLatestSnapshot(String ciProjectId, String ciBuildId, boolean subTree);
}
