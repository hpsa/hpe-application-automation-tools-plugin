// (C) Copyright 2003-2015 Hewlett-Packard Development Company, L.P.

package com.hp.octane.plugins.jetbrains.teamcity.tests.model;

import java.io.Serializable;


import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;


@XmlRootElement(name = "test_run")
final public class TestResult implements Serializable {

    @XmlAttribute(name = "module")
    private String moduleName;

    @XmlAttribute(name = "package")
    private String packageName;

    @XmlAttribute(name = "class")
    private String className;

    @XmlAttribute(name = "name")
    private String testName;

    @XmlAttribute(name = "status")
    private TestResultStatus result;

    @XmlAttribute(name = "duration")
    private String duration;

//    @JacksonXmlProperty(isAttribute = true, localName = "started")
//    private String started;


    public TestResult(String moduleName, String packageName, String className, String testName, String duration, TestResultStatus result) {
        this.moduleName = moduleName;
        this.packageName = packageName;
        this.className = className;
        this.testName = testName;
        this.result = result;
        this.duration = duration;
       // this.started = started;
    }

    public String getModuleName() {
        return moduleName;
    }

    public String getPackageName() {
        return packageName;
    }

    public String getClassName() {
        return className;
    }

    public String getTestName() {
        return testName;
    }

    public TestResultStatus getResult() {
        return result;
    }

    public String getDuration() {
        return duration;
    }

//    public String getStarted() {
//        return started;
//    }
}
