package com.hp.nga.integrations.dto;

import com.hp.nga.integrations.dto.configuration.NGAConfiguration;
import com.hp.nga.integrations.dto.configuration.NGAConfigurationImpl;
import com.hp.nga.integrations.dto.general.AggregatedInfoImpl;
import com.hp.nga.integrations.dto.general.AggregatedInfo;
import com.hp.nga.integrations.dto.general.JobConfig;
import com.hp.nga.integrations.dto.general.JobConfigImpl;
import com.hp.nga.integrations.dto.general.PluginInfo;
import com.hp.nga.integrations.dto.general.ServerInfo;
import com.hp.nga.integrations.dto.general.PluginInfoImpl;
import com.hp.nga.integrations.dto.general.ServerInfoImpl;
import com.hp.nga.integrations.dto.pipelines.BuildHistory;
import com.hp.nga.integrations.dto.pipelines.BuildHistoryImpl;
import com.hp.nga.integrations.dto.pipelines.StructureItem;
import com.hp.nga.integrations.dto.pipelines.StructureItemImpl;
import com.hp.nga.integrations.dto.general.JobsList;
import com.hp.nga.integrations.dto.general.JobsListImpl;
import com.hp.nga.integrations.dto.snapshots.SnapshotItem;
import com.hp.nga.integrations.dto.snapshots.SnapshotItemImpl;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by gullery on 08/02/2016.
 * <p>
 * Generator of the DTOs
 */

public class DTOFactory {
	public static final DTOFactory instance = new DTOFactory();
	private final Map<Class, Class> dtoPairs = new HashMap<Class, Class>();

	private DTOFactory() {
		dtoPairs.put(PluginInfo.class, PluginInfoImpl.class);
		dtoPairs.put(ServerInfo.class, ServerInfoImpl.class);
		dtoPairs.put(AggregatedInfo.class, AggregatedInfoImpl.class);
		dtoPairs.put(JobConfig.class, JobConfigImpl.class);
		dtoPairs.put(JobsList.class, JobsListImpl.class);
		dtoPairs.put(StructureItem.class, StructureItemImpl.class);
		dtoPairs.put(SnapshotItem.class, SnapshotItemImpl.class);
		dtoPairs.put(NGAConfiguration.class, NGAConfigurationImpl.class);
		dtoPairs.put(BuildHistory.class, BuildHistoryImpl.class);
	}

	public <T> T createDTO(Class<T> targetType) {
		if (targetType == null) {
			throw new IllegalArgumentException("target type MUST NOT be null");
		}
		if (!targetType.isInterface()) {
			throw new IllegalArgumentException("target type MUST BE an Interface");
		}
		if (!dtoPairs.containsKey(targetType)) {
			throw new IllegalArgumentException("target type if not one of supported ones");
		}

		try {
			return (T) dtoPairs.get(targetType).newInstance();
		} catch (InstantiationException ie) {
			throw new RuntimeException("failed to instantiate DTO of type " + targetType);
		} catch (IllegalAccessException iae) {
			throw new RuntimeException("DTO of type " + targetType + " is inaccessible");
		}
	}
}
