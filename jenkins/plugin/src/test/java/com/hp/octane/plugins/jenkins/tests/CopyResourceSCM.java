package com.hp.octane.plugins.jenkins.tests;// (C) Copyright 2003-2015 Hewlett-Packard Development Company, L.P.

import hudson.FilePath;
import hudson.Launcher;
import hudson.model.AbstractBuild;
import hudson.model.BuildListener;
import hudson.scm.NullSCM;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

import java.io.File;
import java.io.IOException;

public class CopyResourceSCM extends NullSCM {

    private String path;

    public CopyResourceSCM(String path) {
        this.path = path;
    }

    @Override
    public boolean checkout(AbstractBuild<?,?> build, Launcher launcher, FilePath workspace, BuildListener listener, File changeLogFile) throws IOException, InterruptedException {
        if (workspace.exists()) {
            listener.getLogger().println("Deleting existing workspace " + workspace.getRemote());
            workspace.deleteRecursive();
        }
        Resource[] resources = new PathMatchingResourcePatternResolver().getResources("classpath*:" + path + "/**");
        for (Resource resource : resources) {
            if (resource.exists() && resource.isReadable()) {
                String urlString = resource.getURL().toExternalForm();
                String targetName = urlString.substring(urlString.indexOf(path) + path.length());
                byte[] fileContent = IOUtils.toByteArray(resource.getInputStream());
                FileUtils.writeByteArrayToFile(new File(workspace.getRemote(), targetName), fileContent);
            }
        }
        return true;
    }
}
