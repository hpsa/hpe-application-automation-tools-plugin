package com.hp.nga.integrations.api;

import com.hp.nga.integrations.dto.builds.SnapshotDTO;
import com.hp.nga.integrations.dto.general.PluginInfo;
import com.hp.nga.integrations.dto.general.ServerInfo;

/**
 * Created by gullery on 20/01/2016.
 * <p>
 * API definition of a CI Server data provider for NGA
 */

public abstract class CIDataProvider {
	private static final Object INSTANCE_LOCK = new Object();
	private static volatile CIDataProvider instance;

	public CIDataProvider() {
		synchronized (INSTANCE_LOCK) {
			if (instance == null) {
				instance = this;
			} else {
				throw new RuntimeException("more than one instance of CIServerDataProvider is not allowed");
			}
		}
	}

	public static CIDataProvider getInstance() {
		if (instance != null) {
			return instance;
		} else {
			throw new RuntimeException("CIServerDataProvider MUST NOT be used prior to initialization");
		}
	}

	/**
	 * Provisioning of CI Server information
	 *
	 * @return ServerInfo object
	 */
	public abstract ServerInfo getServerInfo();

	/**
	 * Provisioning of Plugin's information
	 *
	 * @return PluginInfo object
	 */
	public abstract PluginInfo getPluginInfo();

	//  TODO: projects list
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
	public abstract SnapshotDTO getLatestSnapshot(String ciProjectId, String ciBuildId, boolean subTree);
}
