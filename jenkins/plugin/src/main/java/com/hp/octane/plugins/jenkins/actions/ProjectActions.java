package com.hp.octane.plugins.jenkins.actions;

import hudson.model.AbstractProject;
import hudson.model.ProminentProjectAction;
import com.hp.octane.plugins.jenkins.commons.Serializer;
import jenkins.util.TimeDuration;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;

import javax.servlet.ServletException;
import java.io.IOException;

/**
 * Created with IntelliJ IDEA.
 * User: gullery
 * Date: 12/08/14
 * Time: 11:41
 * To change this template use File | Settings | File Templates.
 */
public class ProjectActions implements ProminentProjectAction {

	AbstractProject project;

	public ProjectActions(AbstractProject project) {
		this.project = project;
	}

	public String getIconFileName() {
		return null;
	}

	public String getDisplayName() {
		return null;
	}

	public String getUrlName() {
		return "hpDevopsApi";
	}

	public void doStructure(StaplerRequest req, StaplerResponse res) throws IOException, ServletException {
		res.getOutputStream().println(Serializer.getJSON(project).toString());
		res.flushBuffer();
	}

	public void doRun(StaplerRequest req, StaplerResponse res) throws IOException, ServletException {
		//  TODO: add support for parametrized delay (pass it in the request)
		project.doBuild(req, res, new TimeDuration(10));
	}
}
