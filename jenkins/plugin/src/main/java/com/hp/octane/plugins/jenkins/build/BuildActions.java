package com.hp.octane.plugins.jenkins.build;

import hudson.model.AbstractBuild;
import hudson.model.Run;
import jenkins.model.RunAction2;
import com.hp.octane.plugins.jenkins.commons.Serializer;
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
public class BuildActions implements RunAction2 {

	AbstractBuild build;

	public BuildActions(Run run) {
		build = (AbstractBuild) run;
	}

	public void onAttached(Run<?, ?> run) {
	}

	public void onLoad(Run<?, ?> run) {
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

	public void doSnapshot(StaplerRequest req, StaplerResponse res) throws IOException, ServletException {
		res.getOutputStream().println(Serializer.getJSON(build).toString());
		res.flushBuffer();
	}
}
