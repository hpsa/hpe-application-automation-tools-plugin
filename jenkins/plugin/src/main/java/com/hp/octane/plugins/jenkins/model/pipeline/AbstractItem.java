package com.hp.octane.plugins.jenkins.model.pipeline;

import com.hp.octane.plugins.jenkins.model.pipeline.utils.*;
import hudson.model.AbstractProject;
import hudson.model.ParameterDefinition;
import hudson.model.ParametersDefinitionProperty;
import org.kohsuke.stapler.export.Exported;
import org.kohsuke.stapler.export.ExportedBean;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: gullery
 * Date: 12/01/15
 * Time: 10:46
 * To change this template use File | Settings | File Templates.
 */

@ExportedBean
public abstract class AbstractItem {
	private String name;

	protected AbstractItem(String name) {
		this.name = name;
	}

	abstract ParameterConfig[] provideParameters();

	abstract AbstractPhase[] providePhasesInternal();

	abstract AbstractPhase[] providePhasesPostBuilds();

	@Exported(inline = true)
	public String getName() {
		return name;
	}

	@Exported(inline = true)
	public ParameterConfig[] getParameters() {
		return provideParameters();
	}

	@Exported(inline = true)
	public AbstractPhase[] getPhasesInternal() {
		return providePhasesInternal();
	}

	@Exported(inline = true)
	public AbstractPhase[] getPhasesPostBuild() {
		return providePhasesPostBuilds();
	}

	protected ParameterConfig[] getParameterConfigs(AbstractProject project) {
		ParameterConfig[] parameters;
		List<ParameterDefinition> paramDefinitions;
		if (project.isParameterized()) {
			paramDefinitions = ((ParametersDefinitionProperty) project.getProperty(ParametersDefinitionProperty.class)).getParameterDefinitions();
			parameters = new ParameterConfig[paramDefinitions.size()];
			for (int i = 0; i < parameters.length; i++) {
				parameters[i] = new ParameterConfig(paramDefinitions.get(i));
			}
		} else {
			parameters = new ParameterConfig[0];
		}
		return parameters;
	}

	protected AbstractProjectProcessor getFlowProcessor(AbstractProject project) {
		AbstractProjectProcessor flowProcessor = null;
		if (project.getClass().getName().compareTo("hudson.model.FreeStyleProject") == 0) {
			flowProcessor = new FreeStyleProjectProcessor(project);
		} else if (project.getClass().getName().compareTo("hudson.matrix.MatrixProject") == 0) {
			flowProcessor = new MatrixProjectProcessor(project);
		} else if (project.getClass().getName().compareTo("hudson.maven.MavenModuleSet") == 0) {
			flowProcessor = new MavenProjectProcessor(project);
		} else if (project.getClass().getName().compareTo("com.tikal.jenkins.plugins.multijob.MultiJobProject") == 0) {
			flowProcessor = new MultiJobProjectProcessor(project);
		} else {
			flowProcessor = new UnsupportedProjectProcessor();
		}
		return flowProcessor;
	}
}
