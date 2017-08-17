// (C) Copyright 2003-2015 Hewlett-Packard Development Company, L.P.

package com.hpe.application.automation.tools.octane.tests.build;

import hudson.ExtensionList;
import hudson.ExtensionPoint;
import hudson.model.Hudson;
import hudson.model.Run;

abstract class BuildHandlerExtension implements ExtensionPoint {

    public abstract boolean supports(Run<?, ?> build);

    public abstract BuildDescriptor getBuildType(Run<?, ?> build);

    public abstract String getProjectFullName(Run<?, ?> build);

    public static ExtensionList<BuildHandlerExtension> all() {
        return Hudson.getInstance().getExtensionList(BuildHandlerExtension.class);
    }
}
