// (C) Copyright 2003-2015 Hewlett-Packard Development Company, L.P.

package com.hp.octane.plugins.jenkins.tests.junit;

import hudson.maven.MavenBuild;
import hudson.maven.MavenModuleSetBuild;
import hudson.model.AbstractBuild;
import hudson.model.Run;

import java.util.Collection;
import java.util.List;

public class MavenSetModuleDetection extends AbstractMavenModuleDetection {

    public MavenSetModuleDetection(Run build) {
        super(build);
    }

    protected void addPomDirectories(Run build) {
        if (build instanceof MavenModuleSetBuild) {
            Collection<List<MavenBuild>> builds = ((MavenModuleSetBuild) build).getModuleBuilds().values();
            for (List<MavenBuild> builds1 : builds) {
                for (MavenBuild build2 : builds1) {
                    addPomDirectory(build2.getWorkspace());
                }
            }
        }
    }
}
