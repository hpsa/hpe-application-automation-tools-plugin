package com.hp.octane.plugins.jenkins.model.processors.projects;

import com.hp.octane.integrations.dto.DTOFactory;
import com.hp.octane.integrations.dto.api.configuration.OctaneConfiguration;
import com.hp.octane.plugins.jenkins.OctanePlugin;
import com.hp.octane.plugins.jenkins.configuration.ServerConfiguration;
import hudson.model.Cause;
import hudson.model.Job;
import hudson.tasks.Builder;
import jenkins.model.Jenkins;
import net.sf.json.JSONObject;
import org.jenkinsci.plugins.workflow.job.WorkflowJob;

import java.util.ArrayList;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: gullery
 * Date: 24/12/14
 * Time: 13:40
 * To change this template use File | Settings | File Templates.
 */

public class WorkFlowJobProcessor extends AbstractProjectProcessor<WorkflowJob> {
	private static final DTOFactory dtoFactory = DTOFactory.getInstance();

	WorkFlowJobProcessor(Job job) {
		super((WorkflowJob) job);
	}

	public List<Builder> tryGetBuilders() {
		return new ArrayList<>();
	}

	public void scheduleBuild(String parametersBody) {
		int delay = this.job.getQuietPeriod();

		if (parametersBody != null && !parametersBody.isEmpty()) {
			JSONObject bodyJSON = JSONObject.fromObject(parametersBody);

			//  delay
			if (bodyJSON.has("delay") && bodyJSON.get("delay") != null) {
				delay = bodyJSON.getInt("delay");
			}

			//  TODO: support parameters
		}
		this.job.scheduleBuild(delay, new Cause.RemoteCause(getOctaneConfiguration() == null ? "non available URL" : getOctaneConfiguration().getUrl(), "octane driven execution"));
	}

	private OctaneConfiguration getOctaneConfiguration() {
		OctaneConfiguration result = null;
		ServerConfiguration serverConfiguration = Jenkins.getInstance().getPlugin(OctanePlugin.class).getServerConfiguration();
		if (serverConfiguration.location != null && !serverConfiguration.location.isEmpty() &&
				serverConfiguration.sharedSpace != null && !serverConfiguration.sharedSpace.isEmpty()) {
			result = dtoFactory.newDTO(OctaneConfiguration.class)
					.setUrl(serverConfiguration.location)
					.setSharedSpace(serverConfiguration.sharedSpace)
					.setApiKey(serverConfiguration.username)
					.setSecret(serverConfiguration.password);
		}
		return result;
	}
}
