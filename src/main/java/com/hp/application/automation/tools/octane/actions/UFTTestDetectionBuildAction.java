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

package com.hp.application.automation.tools.octane.actions;

import com.hp.application.automation.tools.octane.actions.dto.AutomatedTest;
import hudson.model.AbstractBuild;
import hudson.model.Action;
import hudson.model.Run;

import java.util.List;


public class UFTTestDetectionBuildAction implements Action {
    private AbstractBuild<?, ?> build;


    private UFTTestDetectionResult results;

    @Override
    public String getIconFileName() {
        return "notepad.png";
    }

    @Override
    public String getDisplayName() {
        return "HP Octane UFT Tests Scanner Report";
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

    public boolean getHasNewTests() {
        return results.getNewTests().size() > 0;
    }

    public boolean getHasDeletedTests() {
        return results.getDeletedTests().size() > 0;
    }

    public List<AutomatedTest> getNewTests() {
        return results.getNewTests();
    }

    public void setResults(UFTTestDetectionResult results) {
        this.results = results;
    }
}