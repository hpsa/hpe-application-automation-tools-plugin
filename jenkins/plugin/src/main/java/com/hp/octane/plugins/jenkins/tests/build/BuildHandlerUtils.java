// (C) Copyright 2003-2015 Hewlett-Packard Development Company, L.P.

package com.hp.octane.plugins.jenkins.tests.build;

import hudson.model.AbstractBuild;

public class BuildHandlerUtils {

	public static BuildDescriptor getBuildType(AbstractBuild<?, ?> build) {
		for (BuildHandlerExtension ext : BuildHandlerExtension.all()) {
			if (ext.supports(build)) {
				return ext.getBuildType(build);
			}
		}
		return new BuildDescriptor(
				build.getProject().getName(),
				build.getProject().getName(),
				String.valueOf(build.getNumber()),
				String.valueOf(build.getNumber()),
				"");
	}

	public static String getProjectFullName(AbstractBuild<?, ?> build) {
		for (BuildHandlerExtension ext : BuildHandlerExtension.all()) {
			if (ext.supports(build)) {
				return ext.getProjectFullName(build);
			}
		}
		return build.getProject().getName();
	}
}
