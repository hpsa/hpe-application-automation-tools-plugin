package com.hp.octane.plugins.jetbrains.teamcity.factories;

import com.hp.nga.integrations.dto.projects.JobsListDTO;
import com.hp.octane.plugins.jetbrains.teamcity.model.pipeline.StructureItem;
import com.hp.octane.plugins.jetbrains.teamcity.model.snapshots.SnapshotItem;

/**
 * Created by lazara on 04/01/2016.
 */
public interface ModelFactory {

    JobsListDTO CreateProjectList();

    StructureItem createStructure(String buildConfigurationId);

    SnapshotItem createSnapshot(String buildConfigurationId);
}
