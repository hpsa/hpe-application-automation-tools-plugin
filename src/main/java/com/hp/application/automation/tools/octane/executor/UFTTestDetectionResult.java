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

package com.hp.application.automation.tools.octane.executor;

import com.hp.application.automation.tools.octane.actions.dto.AutomatedTest;
import com.hp.application.automation.tools.octane.actions.dto.ScmResourceFile;

import javax.xml.bind.annotation.*;
import java.util.ArrayList;
import java.util.List;

/**
 * This file represents result of UFT detection files (tests and data tables)
 */
@XmlRootElement(name="detectionResult")
@XmlAccessorType(XmlAccessType.FIELD)
public class UFTTestDetectionResult {


    @XmlElementWrapper(name="newTests")
    @XmlElement(name="test")
    private List<AutomatedTest> newTests = new ArrayList<>();

    @XmlElementWrapper(name="deletedTests")
    @XmlElement(name="test")
    private List<AutomatedTest> deletedTests = new ArrayList<>();

    @XmlElementWrapper(name="updatedTests")
    @XmlElement(name="test")
    private List<AutomatedTest> updatedTests = new ArrayList<>();

    @XmlElementWrapper(name="newScmResources")
    @XmlElement(name="scmResource")
    private List<ScmResourceFile> newScmResourceFiles = new ArrayList<>();

    @XmlElementWrapper(name="deletedScmResources")
    @XmlElement(name="scmResource")
    private List<ScmResourceFile> deletedScmResourceFiles = new ArrayList<>();

    @XmlElementWrapper(name="updatedScmResources")
    @XmlElement(name="scmResource")
    private List<ScmResourceFile> updatedScmResourceFiles = new ArrayList<>();


    @XmlAttribute
    private String scmResourceId;

    @XmlAttribute
    private String workspaceId;

    @XmlAttribute
    private boolean initialDetection;

    public List<AutomatedTest> getNewTests() {
        return newTests;
    }

    public List<AutomatedTest> getDeletedTests() {
        return deletedTests;
    }

    public List<AutomatedTest> getUpdatedTests() {
        return updatedTests;
    }

    public String getScmResourceId() {
        return scmResourceId;
    }

    public void setScmResourceId(String scmResourceId) {
        this.scmResourceId = scmResourceId;
    }

    public String getWorkspaceId() {
        return workspaceId;
    }

    public void setWorkspaceId(String workspaceId) {
        this.workspaceId = workspaceId;
    }

    public boolean isInitialDetection() {
        return initialDetection;
    }

    public void setInitialDetection(boolean initialDetection) {
        this.initialDetection = initialDetection;
    }

    public boolean hasChanges(){
        return !getNewTests().isEmpty() || !getUpdatedTests().isEmpty();
    }

    public List<ScmResourceFile> getNewScmResourceFiles() {
        return newScmResourceFiles;
    }

    public List<ScmResourceFile> getDeletedScmResourceFiles() {
        return deletedScmResourceFiles;
    }

    public List<ScmResourceFile> getUpdatedScmResourceFiles() {
        return updatedScmResourceFiles;
    }

}