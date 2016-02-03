package com.hp.nga.integrations.api;

import com.hp.nga.integrations.dto.general.PluginInfoDTO;
import com.hp.nga.integrations.dto.general.ServerInfoDTO;
import com.hp.nga.integrations.dto.pipelines.BuildHistory;
import com.hp.nga.integrations.dto.pipelines.StructureItem;
import com.hp.nga.integrations.dto.projects.JobsListDTO;
import com.hp.nga.integrations.dto.snapshots.SnapshotItem;
import com.hp.nga.integrations.services.configuration.NGAConfiguration;

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
	JobsListDTO getJobsList(boolean includeParameters);

	/**
	 * Provides Pipeline (structure) from the root CI Job
	 *
	 * @param rootCIJobId
	 * @return
	 */
	StructureItem getPipeline(String rootCIJobId);

	/**
	 * Executes the Pipeline, running the root job
	 *
	 * @param ciJobId
	 * @param originalBody
	 * @return status of execution                              //  [YG]: TODO: this should be normalized in the SDK and not on the plugin level
	 */
	int runPipeline(String ciJobId, String originalBody);       //  [YG]: TODO: replace the body thing with parsed parameters/DTO

	//  TODO: Almog's history API (TBR)

	/**
	 * Provides Snapshot of the specified CI Build
	 *
	 * @param ciJobId
	 * @param subTree
	 * @return
	 */
	SnapshotItem getSnapshotLatest(String ciJobId, boolean subTree);

	BuildHistory getHistoryPipeline(String ciJobId,String originalBody);
}
