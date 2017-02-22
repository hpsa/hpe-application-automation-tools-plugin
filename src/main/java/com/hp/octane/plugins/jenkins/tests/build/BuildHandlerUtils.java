// (C) Copyright 2003-2015 Hewlett-Packard Development Company, L.P.

package com.hp.octane.plugins.jenkins.tests.build;

import com.hp.octane.plugins.jenkins.workflow.WorkflowBuildAdapter;
import com.hp.octane.plugins.jenkins.workflow.WorkflowGraphListener;
import hudson.FilePath;
import hudson.model.AbstractBuild;
import hudson.model.Run;
import org.jenkinsci.plugins.workflow.job.WorkflowRun;

import java.util.ArrayList;
import java.util.List;

public class BuildHandlerUtils {

	public static BuildDescriptor getBuildType(Run<?, ?> build) {
		for (BuildHandlerExtension ext : BuildHandlerExtension.all()) {
			if (ext.supports(build)) {
				return ext.getBuildType(build);
			}
		}
		return new BuildDescriptor(
				build.getParent().getName(),
				build.getParent().getName(),
				String.valueOf(build.getNumber()),
				String.valueOf(build.getNumber()),
				"");
	}

	public static String getProjectFullName(Run<?, ?> build) {
		for (BuildHandlerExtension ext : BuildHandlerExtension.all()) {
			if (ext.supports(build)) {
				return ext.getProjectFullName(build);
			}
		}
		return build.getParent().getName();//builgetProject().getName();
	}

	public static FilePath getWorkspace(Run<?,?> build){
		//this.buildId =/*build.getProject()*/((AbstractProject)build.getParent()).getBuilds().getLastBuild().getId();
			if(build.getExecutor()!=null && build.getExecutor().getCurrentWorkspace()!=null){
				return build.getExecutor().getCurrentWorkspace();
			}
			if (build instanceof AbstractBuild){
				return ((AbstractBuild) build).getWorkspace();
			}
			if(build instanceof WorkflowBuildAdapter){
				return ((WorkflowBuildAdapter)build).getWorkspace();
//				FilePath filePath = new FilePath(new File(((WorkflowRun) build).getParent().getRootDir().
//						getAbsolutePath()+File.separator +"workspace"));
//				return filePath;
			}

			return null;
	}

	public static String getBuildId(Run<?,?> build){
//		if(build instanceof AbstractBuild){
//			return ((AbstractProject)build.getParent()).getBuilds().getLastBuild().getId();
//		}else{
//			return build.getParent().getLastBuild().getId();
//		}
		return build.getParent().getLastBuild().getId();
	}

	public static List<Run> getBuildPerWorkspaces(Run build) {

		if(build instanceof WorkflowRun){
			return  WorkflowGraphListener.FlowNodeContainer.getFlowNode(build);

		}else {
			List<Run> runsList = new ArrayList<>();
			runsList.add(build);
			return runsList;
		}
	}
}
