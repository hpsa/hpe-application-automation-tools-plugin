package com.hp.octane.plugins.jenkins.actions;

import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;

import java.io.IOException;
import java.util.logging.Logger;

/**
 * Created by gullery on 22/12/2015.
 * This Resource is the entry point for all the Builds' related REST APIs under 'octane/projects/<projectRefId>/builds' context
 */

public class BuildsRESTResource {
	private static final Logger logger = Logger.getLogger(BuildsRESTResource.class.getName());
	static final BuildsRESTResource instance = new BuildsRESTResource();

	private BuildsRESTResource() {
	}

	void handle(AbstractProject project, String buildRefId, StaplerRequest req, StaplerResponse res) throws IOException {
		if (buildRefId == null || buildRefId.isEmpty()) {
			res.setStatus(404);
		} else {
			AbstractBuild build = project.getBuild(buildRefId);
			if (build == null) {
				res.setStatus(404);
			} else {
				res.getWriter().write(build.toString());
			}
		}
	}
}
