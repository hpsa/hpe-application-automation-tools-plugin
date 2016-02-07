package com.hp.octane.plugins.jetbrains.teamcity.actions;

import com.hp.nga.integrations.dto.rest.NGAResult;
import com.hp.nga.integrations.dto.rest.NGATask;
import com.hp.nga.integrations.services.bridge.NGATaskProcessor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.UUID;

/**
 * Created by lazara on 07/02/2016.
 */
public class DynamicController extends AbstractActionController {
    @Override
    protected Object buildResults(HttpServletRequest req, HttpServletResponse res) {

            NGATask ngaTask = new NGATask();
            ngaTask.setId(UUID.randomUUID().toString());
            ngaTask.setMethod(req.getMethod());
            ngaTask.setUrl(req.getRequestURI());
            ngaTask.setBody("");
            NGATaskProcessor taskProcessor = new NGATaskProcessor(ngaTask);
            NGAResult result = taskProcessor.execute();
            res.setStatus(result.getStatus());
//            if (result.getBody() != null) {
//                res.getWriter().write(result.getBody());
//            }

        return result.getBody();
    }
}
