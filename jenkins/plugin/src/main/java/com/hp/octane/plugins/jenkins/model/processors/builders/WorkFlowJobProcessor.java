package com.hp.octane.plugins.jenkins.model.processors.builders;

import com.hp.octane.integrations.dto.DTOFactory;
import com.hp.octane.integrations.dto.configuration.OctaneConfiguration;
import com.hp.octane.plugins.jenkins.CIJenkinsServicesImpl;
import com.hp.octane.plugins.jenkins.OctanePlugin;
import com.hp.octane.plugins.jenkins.configuration.ServerConfiguration;
import com.hp.octane.plugins.jenkins.model.processors.projects.AbstractProjectProcessor;
import hudson.model.Cause;
import hudson.model.Job;
import hudson.model.ParametersAction;
import hudson.tasks.Builder;
import jenkins.model.Jenkins;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
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

public class WorkFlowJobProcessor extends AbstractProjectProcessor {
	private static final Logger logger = LogManager.getLogger(CIJenkinsServicesImpl.class);
	private static final DTOFactory dtoFactory = DTOFactory.getInstance();

	final WorkflowJob workflowJob;

	public WorkFlowJobProcessor(Job project) {

		 this.workflowJob = (WorkflowJob) project;
	}

	public List<Builder> tryGetBuilders() {
		return new ArrayList<Builder>();
	}

	public void scheduleBuild(String originalBody)
	{
		int delay = this.workflowJob.getQuietPeriod();
		ParametersAction parametersAction = new ParametersAction();

		if (originalBody != null && !originalBody.isEmpty()) {
			JSONObject bodyJSON = JSONObject.fromObject(originalBody);

			//  delay
			if (bodyJSON.has("delay") && bodyJSON.get("delay") != null) {
				delay = bodyJSON.getInt("delay");
			}

			//  parameters
			if (bodyJSON.has("parameters") && bodyJSON.get("parameters") != null) {
				JSONArray paramsJSON = bodyJSON.getJSONArray("parameters");
			}
		}


		this.workflowJob.scheduleBuild(delay, new Cause.RemoteCause(getOctaneConfiguration() == null ? "non available URL" : getOctaneConfiguration().getUrl(), "octane driven execution"));
	}




	public OctaneConfiguration getOctaneConfiguration() {
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
