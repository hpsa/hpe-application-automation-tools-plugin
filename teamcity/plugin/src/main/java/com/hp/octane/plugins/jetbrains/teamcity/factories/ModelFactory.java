package com.hp.octane.plugins.jetbrains.teamcity.factories;

import com.hp.octane.plugins.jetbrains.teamcity.model.api.ProjectsList;
import com.hp.octane.plugins.jetbrains.teamcity.model.pipeline.TreeItem;

/**
 * Created by lazara on 04/01/2016.
 */
public interface ModelFactory {

    ProjectsList CreateProjectList();

    TreeItem createStructure(String buildConfigurationId);
}
