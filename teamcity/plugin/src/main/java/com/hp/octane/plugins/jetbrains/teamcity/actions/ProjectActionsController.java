package com.hp.octane.plugins.jetbrains.teamcity.actions;

import com.hp.octane.plugins.jetbrains.teamcity.factories.ModelFactory;
import com.hp.octane.plugins.jetbrains.teamcity.model.pipeline.StructureItem;
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
public class ProjectActionsController extends AbstractActionController {

    public ProjectActionsController(SBuildServer server, ProjectManager projectManager,
                                    BuildTypeResponsibilityFacade responsibilityFacade, ModelFactory modelFactory) {
       super(server,projectManager,responsibilityFacade,modelFactory);
    }

    @Override
    public ModelAndView handleRequest(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) throws Exception {

        String buildConfigurationId = httpServletRequest.getParameter("id");
        StructureItem treeRoot =  this.modelFactory.createStructure(buildConfigurationId);

        if(treeRoot !=null) {
            Utils.updateResponse(treeRoot, httpServletRequest, httpServletResponse);
        }else{
            //should update the response?
        }
        return null;
    }

}
