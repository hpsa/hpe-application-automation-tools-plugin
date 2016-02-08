package com.hp.nga.integrations.dto;

import com.hp.nga.integrations.dto.general.AggregatedInfo;
import com.hp.nga.integrations.dto.general.IAggregatedInfo;
import com.hp.nga.integrations.dto.general.IPluginInfo;
import com.hp.nga.integrations.dto.general.IServerInfo;
import com.hp.nga.integrations.dto.general.PluginInfo;
import com.hp.nga.integrations.dto.general.ServerInfo;
import com.hp.nga.integrations.dto.pipelines.BuildHistory;
import com.hp.nga.integrations.dto.pipelines.BuildHistoryImpl;
import com.hp.nga.integrations.dto.pipelines.StructureItem;
import com.hp.nga.integrations.dto.pipelines.StructureItemImpl;
import com.hp.nga.integrations.dto.projects.JobsListDTO;
import com.hp.nga.integrations.dto.projects.JobsListDTOImpl;
import com.hp.nga.integrations.dto.snapshots.SnapshotItem;
import com.hp.nga.integrations.dto.snapshots.SnapshotItemImpl;


/**
 * Created by gullery on 08/02/2016.
 * <p>
 * Generator of the DTOs
 */

public enum DTOFactory {
	PLUGIN_INFO(IPluginInfo.class, PluginInfo.class),
	SERVER_INFO(IServerInfo.class, ServerInfo.class),
	STRUCTURE_ITEM(StructureItem.class, StructureItemImpl.class),
	SNAPSHOT_ITEM(SnapshotItem.class, SnapshotItemImpl.class),
	JOB_LIST_DTO(JobsListDTO.class, JobsListDTOImpl.class),
	BUILD_HISTORY(BuildHistory.class, BuildHistoryImpl.class),
	AGGREGATED_INFO(IAggregatedInfo.class, AggregatedInfo.class);

	Class dtoInterface;
	Class dtoImplementation;

	DTOFactory(Class type, Class impl) {
		dtoInterface = type;
		dtoImplementation = impl;
	}

	public static <T> T createDTO(Class<T> targetType) {
		if (targetType == null) {
			throw new IllegalArgumentException("target type MUST NOT be null");
		}
		if (!targetType.isInterface()) {
			throw new IllegalArgumentException("target type MUST BE an Interface");
		}

		Object result = null;
		for (DTOFactory type : values()) {
			if (type.dtoInterface.equals(targetType)) {
				try {
					result = type.dtoImplementation.newInstance();
				} catch (Exception e) {
					throw new RuntimeException("failed to instantiate DTO of type " + targetType);
				}
			}
		}

		if (result != null) {
			return (T) result;
		} else {
			throw new RuntimeException("DTO of type " + targetType + " is not supported");
		}
	}


	/*
	package com.hp.nga.integrations.api;


	public interface CIPluginServices {

		NGAConfiguration getNGAConfiguration();
		int runPipeline(String ciJobId, String originalBody);       //  [YG]: TODO: replace the body thing with parsed parameters/DTO
	* */
}
