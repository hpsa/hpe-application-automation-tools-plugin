package com.hp.octane.plugins.jenkins.rest;

import com.hp.octane.plugins.jenkins.model.snapshots.SnapshotItem;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;
import org.kohsuke.stapler.export.Flavor;

import javax.servlet.ServletException;
import java.io.IOException;
import java.util.logging.Logger;

/**
 * Created by gullery on 22/12/2015.
 * This Resource is the entry point for all the Builds' related REST APIs under 'octane/projects/<projectRefId>/builds' context
 */

public class BuildsRESTResource {
	private static final Logger logger = Logger.getLogger(BuildsRESTResource.class.getName());
	public static final BuildsRESTResource instance = new BuildsRESTResource();

	private static final String LATEST_BUILD_ALIAS = "latest";
	private static final String RANGE_BUILDS_ALIAS = "range";
	private static final String SCM_REST = "scm";
	private static final String TESTS_REST = "tests";
	private static final String COVERAGE_REST = "coverage";

	private BuildsRESTResource() {
	}

	void handle(AbstractProject project, String buildRefId, StaplerRequest req, StaplerResponse res) throws IOException, ServletException {
		if (buildRefId == null || buildRefId.isEmpty()) {
			res.setStatus(404);
		} else {
			AbstractBuild build;
			String[] path = req.getRestOfPath().substring(req.getRestOfPath().indexOf(buildRefId) + buildRefId.length()).split("/");

			if (LATEST_BUILD_ALIAS.equals(buildRefId)) {
				build = project.getLastBuild();
				serveBuild(build, path, req, res);
			} else if (RANGE_BUILDS_ALIAS.equals(buildRefId)) {
				//  TODO: get the range of the builds
			} else {
				int buildNumberId;
				try {
					buildNumberId = Integer.parseInt(buildRefId);
					build = project.getBuildByNumber(buildNumberId);
					serveBuild(build, path, req, res);
				} catch (NumberFormatException nfe) {
					logger.severe("failed to parse build ref id '" + buildRefId + "'; integer expected");
					res.setStatus(400);
					res.getWriter().write("failed to parse build ref id: " + nfe.getMessage());
				}
			}
		}
	}

	public void serveBuild(AbstractBuild build, String[] path, StaplerRequest req, StaplerResponse res) throws IOException, ServletException {
		if (build == null) {
			res.setStatus(404);
		} else {
			if (path.length == 1) {
				if ("GET".equals(req.getMethod())) {
					String metaonlyParam = req.getParameter("metaonly");
					boolean metaonly = metaonlyParam != null && metaonlyParam.equals("true");
					res.serveExposedBean(req, new SnapshotItem(build, metaonly), Flavor.JSON);
				} else {
					res.setStatus(405);
				}
			} else {
				if (SCM_REST.equals(path[1])) {
					if ("GET".equals(path[1])) {
						res.getWriter().write("serve SCM data here");
					} else {
						res.setStatus(405);
					}
				} else if (TESTS_REST.equals(path[1])) {
					if ("GET".equals(req.getMethod())) {
						res.getWriter().write("serve tests here");
					} else {
						res.setStatus(405);
					}
				} else if (COVERAGE_REST.equals(path[1])) {
					if ("GET".equals(req.getMethod())) {
						res.getWriter().write("[]");
					} else {
						res.setStatus(405);
					}
				} else {
					res.setStatus(404);
				}
			}
		}
	}
}
