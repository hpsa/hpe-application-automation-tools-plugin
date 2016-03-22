package com.hp.octane.plugins.jenkins.tests.build;

import com.hp.nga.integrations.dto.parameters.CIParameter;
import com.hp.octane.plugins.jenkins.model.ModelFactory;
import com.hp.octane.plugins.jenkins.model.processors.parameters.ParameterProcessors;
import hudson.Extension;
import hudson.model.AbstractBuild;

import java.util.List;

@Extension
public class MatrixBuildExtension extends BuildHandlerExtension {

    @Override
    public boolean supports(AbstractBuild<?, ?> build) {
        return "hudson.matrix.MatrixRun".equals(build.getClass().getName());
    }

    @Override
    public BuildTypeDescriptor getBuildType(AbstractBuild<?, ?> build) {
        List<CIParameter> parameters = ParameterProcessors.getInstances(build);
        String subBuildName = ModelFactory.generateSubBuildName(parameters);
        return new BuildTypeDescriptor(build.getRootBuild().getProject().getName(), subBuildName);
    }

    @Override
    public String getProjectFullName(AbstractBuild<?, ?> build) {
        return build.getRootBuild().getProject().getName() + "/" + build.getProject().getName();
    }
}
