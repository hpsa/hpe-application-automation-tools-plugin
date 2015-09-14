// (C) Copyright 2003-2015 Hewlett-Packard Development Company, L.P.

package com.hp.octane.plugins.jenkins.tests;

import com.hp.octane.plugins.jenkins.tests.impl.TestResultIterator;
import hudson.ExtensionList;
import hudson.ExtensionPoint;
import hudson.model.AbstractBuild;
import hudson.model.Hudson;

import java.io.IOException;

public abstract class MqmTestsExtension implements ExtensionPoint {

    public abstract boolean supports(AbstractBuild<?, ?> build) throws IOException, InterruptedException;

    public abstract TestResultIterator getTestResults(AbstractBuild<?, ?> build) throws IOException, InterruptedException;

    public static ExtensionList<MqmTestsExtension> all() {
        return Hudson.getInstance().getExtensionList(MqmTestsExtension.class);
    }
}
