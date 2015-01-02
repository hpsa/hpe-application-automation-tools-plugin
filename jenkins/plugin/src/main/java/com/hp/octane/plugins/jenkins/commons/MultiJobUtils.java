package com.hp.octane.plugins.jenkins.commons;

import com.tikal.jenkins.plugins.multijob.MultiJobBuilder;
import com.tikal.jenkins.plugins.multijob.PhaseJobsConfig;
import hudson.model.AbstractProject;
import hudson.tasks.Builder;
import jenkins.model.Jenkins;

import java.util.ArrayList;

/**
 * Created with IntelliJ IDEA.
 * User: gullery
 * Date: 24/12/14
 * Time: 13:40
 * To change this template use File | Settings | File Templates.
 */
public class MultiJobUtils {

	static String retrievePhaseName(Builder builder) {
		MultiJobBuilder mjb = (MultiJobBuilder) builder;
		return mjb.getPhaseName();
	}

	static ArrayList<AbstractProject> retrieveSubSteps(Builder builder) {
		MultiJobBuilder mjb = (MultiJobBuilder) builder;
		ArrayList<AbstractProject> subSteps = new ArrayList<AbstractProject>();
		for (PhaseJobsConfig config : mjb.getPhaseJobs()) {
			subSteps.add((AbstractProject) Jenkins.getInstance().getItem(config.getJobName()));
		}
		return subSteps;
	}
}
