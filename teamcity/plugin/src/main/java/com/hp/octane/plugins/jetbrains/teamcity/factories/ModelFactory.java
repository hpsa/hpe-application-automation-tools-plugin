package com.hp.octane.plugins.jetbrains.teamcity.factories;

import com.hp.octane.plugins.jetbrains.teamcity.model.api.ProjectsList;
import com.hp.octane.plugins.jetbrains.teamcity.model.pipeline.StructureItem;

/**
 * Created by lazara on 04/01/2016.
 */
public interface ModelFactory {

    ProjectsList CreateProjectList();

    StructureItem createStructure(String buildConfigurationId);
}
