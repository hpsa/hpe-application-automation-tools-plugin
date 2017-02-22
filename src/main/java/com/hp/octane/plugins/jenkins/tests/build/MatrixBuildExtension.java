package com.hp.octane.plugins.jenkins.tests.build;

import com.hp.octane.integrations.dto.parameters.CIParameter;
import com.hp.octane.plugins.jenkins.model.ModelFactory;
import com.hp.octane.plugins.jenkins.model.processors.parameters.ParameterProcessors;
import hudson.Extension;
import hudson.model.AbstractBuild;
import hudson.model.Run;

import java.util.List;

@Extension
public class MatrixBuildExtension extends BuildHandlerExtension {

	@Override
	public boolean supports(Run<?, ?> build) {
		return "hudson.matrix.MatrixRun".equals(build.getClass().getName());
	}

	@Override
	public BuildDescriptor getBuildType(Run<?, ?> build) {
		AbstractBuild matrixRun = (AbstractBuild) build;
		List<CIParameter> parameters = ParameterProcessors.getInstances(build);
		String subBuildName = ModelFactory.generateSubBuildName(parameters);
		return new BuildDescriptor(
				matrixRun.getRootBuild().getProject().getName(),
				matrixRun.getRootBuild().getProject().getName(),
				String.valueOf(build.getNumber()),
				String.valueOf(build.getNumber()),
				subBuildName);
	}

	@Override
	public String getProjectFullName(Run<?, ?> build) {
        AbstractBuild matrixRun = (AbstractBuild) build;
		return matrixRun.getRootBuild().getProject().getName() + "/" + matrixRun.getProject().getName();
	}
}
