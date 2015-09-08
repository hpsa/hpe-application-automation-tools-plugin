// (C) Copyright 2003-2015 Hewlett-Packard Development Company, L.P.

package com.hp.octane.plugins.jenkins.tests.build;

import hudson.Extension;
import hudson.model.AbstractBuild;

@Extension
public class MatrixBuildExtension extends BuildHandlerExtension {

    @Override
    public boolean supports(AbstractBuild<?, ?> build) {
        return "hudson.matrix.MatrixRun".equals(build.getClass().getName());
    }

    @Override
    public String getBuildType(AbstractBuild<?, ?> build) {
        return build.getRootBuild().getProject().getName();
    }

    @Override
    public String getProjectFullName(AbstractBuild<?, ?> build) {
        return build.getRootBuild().getProject().getName() + "/" + build.getProject().getName();
    }
}
