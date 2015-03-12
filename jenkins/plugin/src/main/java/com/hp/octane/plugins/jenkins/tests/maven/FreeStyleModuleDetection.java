// (C) Copyright 2003-2015 Hewlett-Packard Development Company, L.P.

package com.hp.octane.plugins.jenkins.tests.maven;

import hudson.FilePath;
import hudson.model.AbstractBuild;
import hudson.model.FreeStyleProject;
import hudson.tasks.Builder;
import hudson.tasks.Maven;

import java.io.File;

public class FreeStyleModuleDetection extends AbstractMavenModuleDetection {

    public FreeStyleModuleDetection(AbstractBuild build) {
        super(build);
    }

    protected void addPomDirectories(AbstractBuild build) {
        if (build.getProject() instanceof FreeStyleProject) {
            for (Builder builder: ((FreeStyleProject) build.getProject()).getBuilders()) {
                if (builder instanceof Maven) {
                    Maven maven = (Maven) builder;
                    if (maven.pom != null) {
                        if (maven.pom.endsWith("/pom.xml") || maven.pom.endsWith("\\pom.xml")) {
                            addPomDirectory(new FilePath(rootDir, maven.pom.substring(0, maven.pom.length() - 8)));
                            continue;
                        } else {
                            int p = maven.pom.lastIndexOf(File.separatorChar);
                            if (p > 0) {
                                addPomDirectory(new FilePath(rootDir, maven.pom.substring(0, p)));
                                continue;
                            }
                        }
                    }
                    addPomDirectory(rootDir);
                }
            }
        }
    }
}
