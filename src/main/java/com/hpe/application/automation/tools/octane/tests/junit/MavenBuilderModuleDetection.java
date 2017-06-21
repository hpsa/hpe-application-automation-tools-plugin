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

import hudson.FilePath;
import hudson.model.AbstractBuild;
import hudson.model.FreeStyleProject;
import hudson.model.Project;
import hudson.model.Run;
import hudson.tasks.Builder;
import hudson.tasks.Maven;

import java.io.File;

public class MavenBuilderModuleDetection extends AbstractMavenModuleDetection {

    public MavenBuilderModuleDetection(Run build) {
        super(build);
    }

    protected void addPomDirectories(Run build) {
        if (build instanceof AbstractBuild) {

            if (((AbstractBuild)build).getProject() instanceof FreeStyleProject ||
                    "hudson.matrix.MatrixConfiguration".equals(((AbstractBuild)build).getProject().getClass().getName())) {
                boolean unknownBuilder = false;
                for (Builder builder : ((Project<?, ?>) ((AbstractBuild)build).getProject()).getBuilders()) {
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
                    } else {
                        unknownBuilder = true;
                    }
                }
                if (unknownBuilder && !pomDirs.contains(rootDir)) {
                    // attempt to support shell and batch executions too
                    // simply assume there is top-level pom file for any non-maven builder
                    addPomDirectory(rootDir);
                }
            }
        }
    }
}
