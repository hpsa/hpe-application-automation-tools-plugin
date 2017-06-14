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

package com.hpe.application.automation.tools.octane.actions;

import com.hpe.application.automation.tools.octane.executor.UFTTestDetectionResult;
import com.hpe.application.automation.tools.octane.executor.UFTTestDetectionService;
import hudson.model.AbstractBuild;
import hudson.model.Action;
import hudson.model.Run;

/**
 * Class responsible to show report of  {@link UFTTestDetectionService}
 */
public class UFTTestDetectionBuildAction implements Action {
    private AbstractBuild<?, ?> build;


    private UFTTestDetectionResult results;

    @Override
    public String getIconFileName() {
        return "notepad.png";
    }

    @Override
    public String getDisplayName() {
        return "HPE ALM Octane UFT Tests Discovery Report";
    }

    @Override
    public String getUrlName() {
        return "uft_report";
    }

    @SuppressWarnings("squid:S1452")
    public final Run<?, ?> getBuild() {
        return build;
    }

    public UFTTestDetectionBuildAction(final AbstractBuild<?, ?> build, UFTTestDetectionResult results) {
        this.build = build;
        this.results = results == null ? new UFTTestDetectionResult() : results;
    }

    public UFTTestDetectionResult getResults() {
        return results;
    }

    /**
     * used by ~\src\main\resources\com\hp\application\automation\tools\octane\actions\UFTTestDetectionBuildAction\index.jelly
     *
     * @return
     */
    public boolean getHasNewTests() {
        return results.getNewTests().size() > 0;
    }

    /**
     * used by ~\src\main\resources\com\hp\application\automation\tools\octane\actions\UFTTestDetectionBuildAction\index.jelly
     *
     * @return
     */
    public boolean getHasDeletedTests() {
        return results.getDeletedTests().size() > 0;
    }

    /**
     * used by ~\src\main\resources\com\hp\application\automation\tools\octane\actions\UFTTestDetectionBuildAction\index.jelly
     *
     * @return
     */
    public boolean getHasUpdatedTests() {
        return results.getUpdatedTests().size() > 0;
    }

    /**
     * used by ~\src\main\resources\com\hp\application\automation\tools\octane\actions\UFTTestDetectionBuildAction\index.jelly
     *
     * @return
     */
    public boolean getHasNewScmResources() {
        return results.getNewScmResourceFiles().size() > 0;
    }

    /**
     * used by ~\src\main\resources\com\hp\application\automation\tools\octane\actions\UFTTestDetectionBuildAction\index.jelly
     *
     * @return
     */
    public boolean getHasDeletedScmResources() {
        return results.getDeletedScmResourceFiles().size() > 0;
    }

    public void setResults(UFTTestDetectionResult results) {
        this.results = results;
    }
}