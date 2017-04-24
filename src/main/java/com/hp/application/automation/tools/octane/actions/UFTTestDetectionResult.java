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

import java.util.ArrayList;
import java.util.List;

public class UFTTestDetectionResult {

    private List<AutomatedTest> newTests = new ArrayList<>();
    private List<AutomatedTest> deletedTests = new ArrayList<>();
    private List<AutomatedTest> updatedTests = new ArrayList<>();
    private boolean postedSuccessfully = false;
    private boolean updatedSuccessfully = false;

    public List<AutomatedTest> getNewTests() {
        return newTests;
    }

    public List<AutomatedTest> getDeletedTests() {
        return deletedTests;
    }

    public boolean isPostedSuccessfully() {
        return postedSuccessfully;
    }

    public void setPostedSuccessfully(boolean postedSuccessfully) {
        this.postedSuccessfully = postedSuccessfully;
    }

    public List<AutomatedTest> getUpdatedTests() {
        return updatedTests;
    }

    public void setUpdatedTests(List<AutomatedTest> updatedTests) {
        this.updatedTests = updatedTests;
    }

    public void setUpdatedSuccessfully(boolean updated) {
        updatedSuccessfully = updated;
    }

    public boolean isUpdatedSuccessfully() {
        return updatedSuccessfully;
    }
}