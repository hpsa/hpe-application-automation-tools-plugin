package com.hp.octane.plugins.jetbrains.teamcity.actions;

import com.hp.octane.plugins.jetbrains.teamcity.factories.ModelFactory;
import jetbrains.buildServer.responsibility.BuildTypeResponsibilityFacade;
import jetbrains.buildServer.serverSide.ProjectManager;
import jetbrains.buildServer.serverSide.SBuildServer;
import org.springframework.web.servlet.mvc.Controller;

/**
 * Created by lazara on 04/01/2016.
 */
abstract class AbstractActionController implements Controller {

//    protected final SBuildServer myServer;
//    protected final ProjectManager projectManager;
//    protected final BuildTypeResponsibilityFacade responsibilityFacade;
    protected ModelFactory modelFactory;

    public AbstractActionController(SBuildServer server,
                                    ProjectManager projectManager,
                                    BuildTypeResponsibilityFacade responsibilityFacade,
                                    ModelFactory modelFactory) {

//        this.myServer = server;
//        this.projectManager = projectManager;
//        this.responsibilityFacade = responsibilityFacade;
        this.modelFactory =modelFactory;
    }
}
