package com.hp.octane.plugins.jetbrains.teamcity.actions;

import com.hp.nga.integrations.dto.snapshots.SnapshotNode;
import com.hp.octane.plugins.jetbrains.teamcity.factories.ModelFactory;
import jetbrains.buildServer.responsibility.BuildTypeResponsibilityFacade;
import jetbrains.buildServer.serverSide.ProjectManager;
import jetbrains.buildServer.serverSide.SBuildServer;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Created by lazara on 27/12/2015.
 */
public class BuildActionsController extends AbstractActionController {

    public BuildActionsController(SBuildServer server, ProjectManager projectManager,
                                  BuildTypeResponsibilityFacade responsibilityFacade) {

    }

    @Override
    protected Object buildResults(HttpServletRequest request, HttpServletResponse response) {
        String buildConfigurationId = request.getParameter("id");
        SnapshotNode snapshotNode = ModelFactory.createSnapshot(buildConfigurationId);
       return snapshotNode;
    }
}
