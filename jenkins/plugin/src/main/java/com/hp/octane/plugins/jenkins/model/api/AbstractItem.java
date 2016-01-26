package com.hp.octane.plugins.jenkins.model.api;

import com.hp.nga.integrations.dto.parameters.ParameterConfig;
import com.hp.octane.plugins.jenkins.model.processors.projects.AbstractProjectProcessor;
import hudson.model.AbstractProject;
import org.kohsuke.stapler.export.Exported;
import org.kohsuke.stapler.export.ExportedBean;

/**
 * Created with IntelliJ IDEA.
 * User: gullery
 * Date: 12/01/15
 * Time: 10:46
 * To change this template use File | Settings | File Templates.
 */

@ExportedBean
public abstract class AbstractItem<TP extends ParameterConfig, TPH extends AbstractPhase> {
	private String name;
	private TP[] parameters;
	private TPH[] internals;
	private TPH[] postBuilds;

	private AbstractProjectProcessor flowProcessor;

	@SuppressWarnings("unchecked")
	protected AbstractItem(AbstractProject project) {
		this.name = project.getName();
		flowProcessor = AbstractProjectProcessor.getFlowProcessor(project);
	}

	@Exported(inline = true)
	public String getName() {
		return name;
	}

	protected void setParameters(TP[] parameters) {
		this.parameters = parameters;
	}

	@Exported(inline = true)
	public TP[] getParameters() {
		return parameters;
	}

	protected void setInternals(TPH[] internals) {
		this.internals = internals;
	}

	@Exported(inline = true, name = "phasesInternal")
	public TPH[] getInternals() {
		return internals;
	}

	protected void setPostBuilds(TPH[] postBuilds) {
		this.postBuilds = postBuilds;
	}

	@Exported(inline = true, name = "phasesPostBuild")
	public TPH[] getPostBuilds() {
		return postBuilds;
	}

	protected AbstractProjectProcessor getFlowProcessor() {
		return flowProcessor;
	}
}
