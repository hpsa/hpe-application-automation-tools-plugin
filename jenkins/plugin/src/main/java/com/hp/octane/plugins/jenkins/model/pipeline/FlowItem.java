package com.hp.octane.plugins.jenkins.model.pipeline;

import com.hp.octane.plugins.jenkins.model.pipeline.utils.*;
import hudson.model.AbstractProject;
import org.kohsuke.stapler.export.Exported;
import org.kohsuke.stapler.export.ExportedBean;

/**
 * Created with IntelliJ IDEA.
 * User: gullery
 * Date: 03/01/15
 * Time: 10:49
 * To change this template use File | Settings | File Templates.
 */

@ExportedBean
public class FlowItem {
	private String name;
	private FlowPhase[] internals;
	private FlowPhase[] postBuilds;

	public FlowItem(AbstractProject project) {
		if (project == null) throw new IllegalArgumentException("project MUST not be null");

		AbstractProjectProcessor flowProcessor = null;
		name = project.getName();

		//  TODO: add scm data handling

		if (project.getClass().getName().compareTo("hudson.model.FreeStyleProject") == 0) {
			flowProcessor = new FreeStyleProjectProcessor(project);
		} else if (project.getClass().getName().compareTo("hudson.matrix.MatrixProject") == 0) {
			flowProcessor = new MatrixProjectProcessor(project);
		} else if (project.getClass().getName().compareTo("hudson.maven.MavenModuleSet") == 0) {
			flowProcessor = new MavenProjectProcessor(project);
		} else if (project.getClass().getName().compareTo("com.tikal.jenkins.plugins.multijob.MultiJobProject") == 0) {
			flowProcessor = new MultiJobProjectProcessor(project);
		}
		if (flowProcessor != null) {
			internals = flowProcessor.getInternals();
			postBuilds = flowProcessor.getPostBuilds();
		} else {
			internals = new FlowPhase[0];
			postBuilds = new FlowPhase[0];
		}
	}

	@Exported(inline = true)
	public String getName() {
		return name;
	}

	@Exported(inline = true)
	public FlowPhase[] getPhasesInternal() {
		return internals;
	}

	@Exported(inline = true)
	public FlowPhase[] getPhasesPostBuild() {
		return postBuilds;
	}
}
