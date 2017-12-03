/*
 * © Copyright 2013 EntIT Software LLC
 *  Certain versions of software and/or documents (“Material”) accessible here may contain branding from
 *  Hewlett-Packard Company (now HP Inc.) and Hewlett Packard Enterprise Company.  As of September 1, 2017,
 *  the Material is now offered by Micro Focus, a separately owned and operated company.  Any reference to the HP
 *  and Hewlett Packard Enterprise/HPE marks is historical in nature, and the HP and Hewlett Packard Enterprise/HPE
 *  marks are the property of their respective owners.
 * __________________________________________________________________
 * MIT License
 *
 * Copyright (c) 2018 Micro Focus Company, L.P.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 * ___________________________________________________________________
 *
 */

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

public class BuildHandlerUtils {

	public static BuildDescriptor getBuildType(Run<?, ?> build) {
		for (BuildHandlerExtension ext : BuildHandlerExtension.all()) {
			if (ext.supports(build)) {
				return ext.getBuildType(build);
			}
		}
		return new BuildDescriptor(
				BuildHandlerUtils.getJobCiId(build),
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
		return build.getParent().getFullName();
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

	public static String getJobCiId(Run r) {
		if (r.getParent() instanceof MatrixConfiguration) {
			return JobProcessorFactory.getFlowProcessor(((MatrixRun) r).getParentBuild().getParent()).getTranslateJobName();
		}
		if (r.getParent().getClass().getName().equals("org.jenkinsci.plugins.workflow.job.WorkflowJob")) {
			return JobProcessorFactory.getFlowProcessor(r.getParent()).getTranslateJobName();
		}
		return JobProcessorFactory.getFlowProcessor(((AbstractBuild) r).getProject()).getTranslateJobName();
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
