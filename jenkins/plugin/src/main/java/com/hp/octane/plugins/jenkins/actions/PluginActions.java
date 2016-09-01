package com.hp.octane.plugins.jenkins.actions;

import com.hp.octane.integrations.OctaneSDK;
import com.hp.octane.integrations.api.TasksProcessor;
import com.hp.octane.integrations.dto.DTOFactory;
import com.hp.octane.integrations.dto.connectivity.HttpMethod;
import com.hp.octane.integrations.dto.connectivity.OctaneResultAbridged;
import com.hp.octane.integrations.dto.connectivity.OctaneTaskAbridged;
import com.hp.octane.plugins.jenkins.configuration.ConfigApi;
import hudson.Extension;
import hudson.model.RootAction;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;

import javax.servlet.ServletException;
import java.io.IOException;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Logger;

/**
 * Created with IntelliJ IDEA.
 * User: gullery
 * Date: 8/10/14
 * Time: 12:47 PM
 * To change this template use File | Settings | File Templates.
 */

@Extension
public class PluginActions implements RootAction {
	private static final Logger logger = Logger.getLogger(PluginActions.class.getName());
	private static final DTOFactory dtoFactory = DTOFactory.getInstance();

	public String getIconFileName() {
		return null;
	}

	public String getDisplayName() {
		return null;
	}

	public String getUrlName() {
		return "nga";
	}

	public ConfigApi getConfiguration() {
		return new ConfigApi();
	}

	public void doDynamic(StaplerRequest req, StaplerResponse res) throws IOException, ServletException {
		HttpMethod method = null;
		if ("post".equals(req.getMethod().toLowerCase())) {
			method = HttpMethod.POST;
		} else if ("get".equals(req.getMethod().toLowerCase())) {
			method = HttpMethod.GET;
		} else if ("put".equals(req.getMethod().toLowerCase())) {
			method = HttpMethod.PUT;
		} else if ("delete".equals(req.getMethod().toLowerCase())) {
			method = HttpMethod.DELETE;
		}
		if (method != null) {
			OctaneTaskAbridged octaneTaskAbridged = dtoFactory.newDTO(OctaneTaskAbridged.class);
			octaneTaskAbridged.setId(UUID.randomUUID().toString());
			octaneTaskAbridged.setMethod(method);
			octaneTaskAbridged.setUrl(req.getRequestURIWithQueryString());
			octaneTaskAbridged.setBody("");
			TasksProcessor taskProcessor = OctaneSDK.getInstance().getTasksProcessor();
			OctaneResultAbridged result = taskProcessor.execute(octaneTaskAbridged);

			res.setStatus(result.getStatus());
			if (result.getBody() != null) {
				res.getWriter().write(result.getBody());
			}
			if (result.getHeaders() != null) {
				for (Map.Entry<String, String> header : result.getHeaders().entrySet()) {
					res.setHeader(header.getKey(), header.getValue());
				}
			}
		} else {
			res.setStatus(501);
		}
	}
}
