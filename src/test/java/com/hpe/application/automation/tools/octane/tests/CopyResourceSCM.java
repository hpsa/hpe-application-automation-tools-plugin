/*
 *     Copyright 2017 Hewlett-Packard Development Company, L.P.
 *     Licensed under the Apache License, Version 2.0 (the "License");
 *     you may not use this file except in compliance with the License.
 *     You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS,
 *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *     See the License for the specific language governing permissions and
 *     limitations under the License.
 *
 */

package com.hpe.application.automation.tools.octane.tests;// (C) Copyright 2003-2015 Hewlett-Packard Development Company, L.P.

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
    private String targetPath;

    public CopyResourceSCM(String path) {
        this(path, "");
    }

    public CopyResourceSCM(String path, String targetPath) {
        this.path = path;
        this.targetPath = targetPath;
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
                FileUtils.writeByteArrayToFile(new File(new File(workspace.getRemote(), targetPath), targetName), fileContent);
            }
        }
        return true;
    }
}
