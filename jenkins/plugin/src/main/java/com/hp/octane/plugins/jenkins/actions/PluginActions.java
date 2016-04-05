package com.hp.octane.plugins.jenkins.actions;

import com.hp.nga.integrations.SDKManager;
import com.hp.nga.integrations.dto.DTOFactory;
import com.hp.nga.integrations.dto.connectivity.NGAHttpMethod;
import com.hp.nga.integrations.dto.connectivity.NGAResultAbridged;
import com.hp.nga.integrations.dto.connectivity.NGATaskAbridged;
import com.hp.nga.integrations.api.TasksProcessor;
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
			ngaTaskAbridged.setUrl(req.getRequestURIWithQueryString());
			ngaTaskAbridged.setBody("");
			TasksProcessor taskProcessor = SDKManager.getService(TasksProcessor.class);
			NGAResultAbridged result = taskProcessor.execute(ngaTaskAbridged);

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
