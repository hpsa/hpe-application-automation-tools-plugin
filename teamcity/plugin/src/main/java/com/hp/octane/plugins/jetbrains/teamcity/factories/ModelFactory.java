package com.hp.octane.plugins.jetbrains.teamcity.factories;

import com.hp.octane.dto.projects.ProjectsList;
import com.hp.octane.plugins.jetbrains.teamcity.model.pipeline.StructureItem;
import com.hp.octane.plugins.jetbrains.teamcity.model.snapshots.SnapshotItem;

/**
 * Created by lazara on 04/01/2016.
 */
public interface ModelFactory {

    ProjectsList CreateProjectList();

    StructureItem createStructure(String buildConfigurationId);

    SnapshotItem createSnapshot(String buildConfigurationId,String buildNumber);
}
