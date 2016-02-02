package com.hp.nga.integrations.api;

import com.hp.nga.integrations.services.configuration.NGAConfiguration;
import com.hp.nga.integrations.dto.general.PluginInfoDTO;
import com.hp.nga.integrations.dto.general.ServerInfoDTO;
import com.hp.nga.integrations.dto.pipelines.StructureItem;
import com.hp.nga.integrations.dto.projects.JobsListDTO;
import com.hp.nga.integrations.dto.snapshots.SnapshotItem;

/**
 * Created by gullery on 20/01/2016.
 * <p>
 * Composite API of all the endpoints to be implemented by a hosting CI Plugin for NGA use cases
 */

public interface CIPluginServices {

	/**
	 * Provides CI Server information
	 *
	 * @return ServerInfo object; MUST NOT return null
	 */
	ServerInfoDTO getServerInfo();

	/**
	 * Provides Plugin's information
	 *
	 * @return PluginInfo object; MUST NOT return null
	 */
	PluginInfoDTO getPluginInfo();

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
	JobsListDTO getProjectsList(boolean includeParameters);

	/**
	 * Provides Pipeline (structure) from the root CI Job
	 *
	 * @param rootCIJobId
	 * @return
	 */
	StructureItem getPipeline(String rootCIJobId);

	//  TODO: project's run
	//  TODO: Almog's history API (TBR)

	/**
	 * Provides Snapshot of the specified CI Build
	 *
	 * @param ciJobId
	 * @param buildId
	 * @param subTree
	 * @return
	 */
	SnapshotItem getSnapshotLatest(String ciJobId, String buildId, boolean subTree);
}
