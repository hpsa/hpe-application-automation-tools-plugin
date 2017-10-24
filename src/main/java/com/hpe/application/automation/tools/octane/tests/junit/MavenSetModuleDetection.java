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
