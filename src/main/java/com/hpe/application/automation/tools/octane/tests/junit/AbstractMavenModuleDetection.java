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

package com.hpe.application.automation.tools.octane.tests.junit;

import com.hpe.application.automation.tools.octane.tests.build.BuildHandlerUtils;
import hudson.FilePath;
import hudson.model.Run;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

public abstract class AbstractMavenModuleDetection implements ModuleDetection {

    protected FilePath rootDir;
    protected List<FilePath> pomDirs;

    public AbstractMavenModuleDetection(Run build) {
        rootDir = BuildHandlerUtils.getWorkspace(build);
        pomDirs = new LinkedList<>();

        addPomDirectories(build);
    }

    protected abstract void addPomDirectories(Run build);

    @Override
    public String getModule(FilePath resultFile) throws IOException, InterruptedException {
        for (FilePath pomDir: pomDirs) {
            if (childOf(pomDir, resultFile)) {
                return normalize(locatePom(resultFile, pomDir));
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

    private String normalize(String path) {
        return path.replace("\\", "/").replaceFirst("^/", "");
    }
}
