package com.hp.octane.plugins.jetbrains.teamcity.actions;

import com.hp.octane.plugins.jetbrains.teamcity.factories.ModelFactory;
import com.hp.octane.plugins.jetbrains.teamcity.model.snapshots.SnapshotItem;
import com.hp.octane.plugins.jetbrains.teamcity.utils.Utils;
import jetbrains.buildServer.responsibility.BuildTypeResponsibilityFacade;
import jetbrains.buildServer.serverSide.ProjectManager;
import jetbrains.buildServer.serverSide.SBuildServer;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Created by lazara on 27/12/2015.
 */
public class BuildActionsController extends AbstractActionController {

    public BuildActionsController(SBuildServer server, ProjectManager projectManager,
                                  BuildTypeResponsibilityFacade responsibilityFacade, ModelFactory modelFactory) {
        super(server,projectManager,responsibilityFacade,modelFactory);

    }

    @Override
    public ModelAndView handleRequest(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) throws Exception {
        String buildConfigurationId = httpServletRequest.getParameter("id");
        SnapshotItem snapshotItem = this.modelFactory.createSnapshot(buildConfigurationId,null);
        Utils.updateResponse(snapshotItem, httpServletRequest, httpServletResponse);

        return null;
    }
}
