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

package com.hp.application.automation.tools.octane.actions.dto;

/**
 * Created by kashbi on 25/09/2016.
 */
public class AutomatedTest {
    private String type = "test";
    private String subtype = "test_automated";
    private ListNodeEntity testingToolType;
    private ListNodeEntity framework;
    private String name;
    private String _package;
    private BaseRefEntity scmRepository;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getSubtype() {
        return subtype;
    }

    public void setSubtype(String subtype) {
        this.subtype = subtype;
    }

    public ListNodeEntity getFramework() {
        return framework;
    }

    public void setFramework(ListNodeEntity framework) {
        this.framework = framework;
    }

    public String getPackage() {
        return _package;
    }

    public void setPackage(String _package) {
        this._package = _package;
    }

    public ListNodeEntity getTestingToolType() {
        return testingToolType;
    }

    public void setTestingToolType(ListNodeEntity testingToolType) {
        this.testingToolType = testingToolType;
    }

    public BaseRefEntity getScmRepository() {
        return scmRepository;
    }

    public void setScmRepository(BaseRefEntity scmRepository) {
        this.scmRepository = scmRepository;
    }
}
