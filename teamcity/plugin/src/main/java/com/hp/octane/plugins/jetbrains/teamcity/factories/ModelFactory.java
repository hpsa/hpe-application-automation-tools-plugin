package com.hp.octane.plugins.jetbrains.teamcity.factories;

import com.hp.octane.plugins.jetbrains.teamcity.model.api.ProjectsList;
import jetbrains.buildServer.serverSide.ProjectManager;

/**
 * Created by lazara on 04/01/2016.
 */
public interface ModelFactory {

    ProjectsList CreateProjectList(ProjectManager projectManager);
}
