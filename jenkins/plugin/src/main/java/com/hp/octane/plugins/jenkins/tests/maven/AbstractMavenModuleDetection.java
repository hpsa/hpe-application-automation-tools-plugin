// (C) Copyright 2003-2015 Hewlett-Packard Development Company, L.P.

package com.hp.octane.plugins.jenkins.tests.maven;

import com.hp.octane.plugins.jenkins.tests.ModuleDetection;
import hudson.FilePath;
import hudson.model.AbstractBuild;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

public abstract class AbstractMavenModuleDetection implements ModuleDetection {

    protected FilePath rootDir;
    private List<FilePath> pomDirs;

    public AbstractMavenModuleDetection(AbstractBuild build) {
        rootDir = build.getWorkspace();
        pomDirs = new LinkedList<FilePath>();

        addPomDirectories(build);
    }

    protected abstract void addPomDirectories(AbstractBuild build);

    public String getModule(FilePath resultFile) throws IOException, InterruptedException {
        for (FilePath pomDir: pomDirs) {
            if (childOf(pomDir, resultFile)) {
                return locatePom(resultFile, pomDir);
            }
        }
        // unable to determine module
        return null;
    }

    protected void addPomDirectory(FilePath pomDir) {
        pomDirs.add(pomDir);
    }

    protected boolean childOf(FilePath parent, FilePath child) {
        while (child != null) {
            if (parent.equals(child)) {
                return true;
            }
            child = child.getParent();
        }
        return false;
    }


    private String locatePom(FilePath filePath, FilePath pomDir) throws IOException, InterruptedException {
        while (filePath != null) {
            FilePath parentPath = filePath.getParent();
            if (parentPath.equals(pomDir)) {
                // walk up as far as the enclosing pom directory
                break;
            }
            FilePath pomPath = new FilePath(parentPath, "pom.xml");
            if (pomPath.exists()) {
                // we found a nested pom directory
                return parentPath.getRemote().substring(rootDir.getRemote().length());
            }
            filePath = parentPath;
        }
        // no other pom found in nested directories
        return pomDir.getRemote().substring(rootDir.getRemote().length());
    }
}
