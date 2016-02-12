package com.hp.octane.plugins.jetbrains.teamcity.actions;

import com.hp.nga.integrations.dto.DTOFactory;
import com.hp.nga.integrations.dto.connectivity.NGAHttpMethod;
import com.hp.nga.integrations.dto.connectivity.NGAResultAbridged;
import com.hp.nga.integrations.dto.connectivity.NGATaskAbridged;
import com.hp.nga.integrations.services.bridge.NGATaskProcessor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.UUID;

/**
 * Created by lazara on 07/02/2016.
 */

public class DynamicController extends AbstractActionController {
	private static final DTOFactory dtoFactory = DTOFactory.getInstance();

	@Override
	protected Object buildResults(HttpServletRequest req, HttpServletResponse res) {
		NGAHttpMethod method = null;
		if ("post".equals(req.getMethod().toLowerCase())) {
			method = NGAHttpMethod.POST;
		} else if ("get".equals(req.getMethod().toLowerCase())) {
			method = NGAHttpMethod.GET;
		} else if ("put".equals(req.getMethod().toLowerCase())) {
			method = NGAHttpMethod.PUT;
		} else if ("delete".equals(req.getMethod().toLowerCase())) {
			method = NGAHttpMethod.DELETE;
		}
		if (method != null) {
			NGATaskAbridged ngaTaskAbridged = dtoFactory.newDTO(NGATaskAbridged.class);
			ngaTaskAbridged.setId(UUID.randomUUID().toString());
			ngaTaskAbridged.setMethod(method);
			ngaTaskAbridged.setUrl(req.getRequestURI());
			ngaTaskAbridged.setBody("");
			NGATaskProcessor taskProcessor = new NGATaskProcessor(ngaTaskAbridged);
			NGAResultAbridged result = taskProcessor.execute();
			res.setStatus(result.getStatus());
			return result.getBody();
		} else {
			res.setStatus(501);
			return "";
		}
	}
}
