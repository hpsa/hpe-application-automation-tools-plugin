// (C) Copyright 2003-2015 Hewlett-Packard Development Company, L.P.

package com.hp.octane.plugins.jenkins.tests;

import hudson.ExtensionList;
import hudson.ExtensionPoint;
import hudson.model.AbstractBuild;
import hudson.model.Hudson;

import java.io.IOException;

public abstract class MqmTestsExtension implements ExtensionPoint {

    public abstract boolean supports(AbstractBuild<?, ?> build) throws IOException, InterruptedException;

    /**
     *
     * @param build build to find test results for
     * @param isStormRunnerProject if the project is storm runner project
     * @return instance of {@link TestResultContainer} or null, when no test results found
     * @throws IOException
     * @throws InterruptedException
     */
    public abstract TestResultContainer getTestResults(AbstractBuild<?, ?> build, boolean isStormRunnerProject) throws IOException, InterruptedException;

    public static ExtensionList<MqmTestsExtension> all() {
        return Hudson.getInstance().getExtensionList(MqmTestsExtension.class);
    }
}
