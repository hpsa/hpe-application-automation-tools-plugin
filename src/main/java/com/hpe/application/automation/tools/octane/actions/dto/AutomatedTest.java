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

package com.hpe.application.automation.tools.octane.actions.dto;

import com.hpe.application.automation.tools.octane.actions.UftTestType;

import javax.xml.bind.annotation.*;

/**
 * This file represents automated test for sending to Octane
 */
@XmlRootElement(name = "test")
@XmlAccessorType(XmlAccessType.FIELD)
public class AutomatedTest {

    @XmlTransient
    private Long id;
    @XmlTransient
    private String type = "test_automated";
    @XmlTransient
    private ListNodeEntity testingToolType;
    @XmlTransient
    private ListNodeEntity framework;
    @XmlTransient
    private ListNodeEntityCollection testTypes;

    //don't serialized to server, used to set testType property
    @XmlAttribute
    private UftTestType uftTestType;

    @XmlAttribute
    private String name;
    @XmlAttribute
    private String packageName;
    @XmlAttribute
    private Boolean executable;

    private String description;

    @XmlTransient
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

    public ListNodeEntity getFramework() {
        return framework;
    }

    public void setFramework(ListNodeEntity framework) {
        this.framework = framework;
    }

    public String getPackage() {
        return packageName;
    }

    public void setPackage(String packageName) {
        this.packageName = packageName;
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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setUftTestType(UftTestType uftTestType) {
        this.uftTestType = uftTestType;
    }

    public UftTestType getUftTestType() {
        return uftTestType;
    }

    public ListNodeEntityCollection getTestTypes() {
        return testTypes;
    }

    public void setTestTypes(ListNodeEntityCollection testTypes) {
        this.testTypes = testTypes;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Boolean getExecutable() {
        return executable;
    }

    public void setExecutable(Boolean executable) {
        this.executable = executable;
    }

    @Override
    public String toString() {
        return "#" + getId() == null ? "0" : getId() + " - " + getPackage() + "@" + getName();
    }
}
