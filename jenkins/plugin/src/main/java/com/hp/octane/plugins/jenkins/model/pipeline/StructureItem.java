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
 * Date: 03/01/15
 * Time: 10:49
 * To change this template use File | Settings | File Templates.
 */

@ExportedBean
public class StructureItem {
	private String name;
	protected ParameterConfig[] parameters;
	protected StructurePhase[] internals;
	protected StructurePhase[] postBuilds;

	public StructureItem(AbstractProject project) {
		AbstractProjectProcessor flowProcessor = null;
		List<ParameterDefinition> paramDefinitions;
		name = project.getName();
		if (project.isParameterized()) {
			paramDefinitions = ((ParametersDefinitionProperty) project.getProperty(ParametersDefinitionProperty.class)).getParameterDefinitions();
			parameters = new ParameterConfig[paramDefinitions.size()];
			for (int i = 0; i < parameters.length; i++) {
				parameters[i] = new ParameterConfig(paramDefinitions.get(i));
			}
		} else {
			parameters = new ParameterConfig[0];
		}

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
			internals = new StructurePhase[0];
			postBuilds = new StructurePhase[0];
		}
	}

	@Exported(inline = true)
	public String getName() {
		return name;
	}

	@Exported(inline = true)
	public ParameterConfig[] getParameters() {
		return parameters;
	}

	@Exported(inline = true)
	public StructurePhase[] getPhasesInternal() {
		return internals;
	}

	@Exported(inline = true)
	public StructurePhase[] getPhasesPostBuild() {
		return postBuilds;
	}
}
