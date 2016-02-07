package com.hp.octane.plugins.jetbrains.teamcity.actions;

import com.hp.octane.plugins.jetbrains.teamcity.utils.Utils;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.Controller;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Created by lazara on 04/01/2016.
 */
abstract class AbstractActionController implements Controller {

//
//    public AbstractActionController(SBuildServer server,
//                                    ProjectManager projectManager,
//                                    BuildTypeResponsibilityFacade responsibilityFacade,
//                                   ) {}

    public  ModelAndView handleRequest(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) throws Exception {
        Object results = buildResults(httpServletRequest,httpServletResponse);

        if(results!=null){
            Utils.updateResponse(results, httpServletRequest, httpServletResponse);

        }
        return null;
    }

    protected abstract Object buildResults(HttpServletRequest request, HttpServletResponse response);

}
