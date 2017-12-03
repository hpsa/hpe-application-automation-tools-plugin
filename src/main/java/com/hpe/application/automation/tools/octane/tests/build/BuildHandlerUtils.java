// (C) Copyright 2003-2015 Hewlett-Packard Development Company, L.P.

package com.hpe.application.automation.tools.octane.tests.build;

import com.hpe.application.automation.tools.octane.model.processors.projects.JobProcessorFactory;
import com.hpe.application.automation.tools.octane.workflow.WorkflowBuildAdapter;
import com.hpe.application.automation.tools.octane.workflow.WorkflowGraphListener;
import hudson.FilePath;
import hudson.matrix.MatrixConfiguration;
import hudson.matrix.MatrixRun;
import hudson.model.AbstractBuild;
import hudson.model.Run;
import org.jenkinsci.plugins.workflow.job.WorkflowRun;

import java.util.ArrayList;
import java.util.List;

/**
 * Generic utilities handling Job/Run metadata extraction/transformation/processing
 */

public class BuildHandlerUtils {

	public static BuildDescriptor getBuildType(Run<?, ?> run) {
		for (BuildHandlerExtension ext : BuildHandlerExtension.all()) {
			if (ext.supports(run)) {
				return ext.getBuildType(run);
			}
		}
		return new BuildDescriptor(
				BuildHandlerUtils.getJobCiId(run),
				run.getParent().getName(),
				BuildHandlerUtils.getBuildCiId(run),
				String.valueOf(run.getNumber()),
				"");
	}

	public static String getProjectFullName(Run<?, ?> run) {
		for (BuildHandlerExtension ext : BuildHandlerExtension.all()) {
			if (ext.supports(run)) {
				return ext.getProjectFullName(run);
			}
		}
		return run.getParent().getFullName();
	}

	public static FilePath getWorkspace(Run<?, ?> run) {
		if (run.getExecutor() != null && run.getExecutor().getCurrentWorkspace() != null) {
			return run.getExecutor().getCurrentWorkspace();
		}
		if (run instanceof AbstractBuild) {
			return ((AbstractBuild) run).getWorkspace();
		}
		if (run instanceof WorkflowBuildAdapter) {
			return ((WorkflowBuildAdapter) run).getWorkspace();
		}
		return null;
	}

	public static List<Run> getBuildPerWorkspaces(Run run) {
		if (run instanceof WorkflowRun) {
			return WorkflowGraphListener.FlowNodeContainer.getFlowNode(run);
		} else {
			List<Run> runsList = new ArrayList<>();
			runsList.add(run);
			return runsList;
		}
	}

	public static String getBuildCiId(Run run) {
		return String.valueOf(run.getNumber());
		//  YG  temportarty disabling the support for fluid build number until Octane supports it
		//return run.getNumber() + "_" + run.getStartTimeInMillis();
	}

	public static String getJobCiId(Run run) {
		if (run.getParent() instanceof MatrixConfiguration) {
			return JobProcessorFactory.getFlowProcessor(((MatrixRun) run).getParentBuild().getParent()).getTranslateJobName();
		}
		if (run.getParent().getClass().getName().equals("org.jenkinsci.plugins.workflow.job.WorkflowJob")) {
			return JobProcessorFactory.getFlowProcessor(run.getParent()).getTranslateJobName();
		}
		return JobProcessorFactory.getFlowProcessor(((AbstractBuild) run).getProject()).getTranslateJobName();
	}

	/**
	 * Retrieve Job's CI ID
	 *
	 * @return Job's CI ID
	 */
	public static String translateFolderJobName(String jobPlainName) {
		String newSplitterCharacters = "/job/";
		return jobPlainName.replaceAll("/", newSplitterCharacters);
	}
}
