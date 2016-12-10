package com.hp.octane.integrations.dto.tests.impl;

import com.hp.octane.integrations.dto.tests.TestCase;

import javax.xml.bind.annotation.*;

/**
 * Created by lev on 31/05/2016.
 */
@XmlRootElement(name = "testcase")
@XmlAccessorType(XmlAccessType.NONE)
public class TestCaseImpl implements TestCase {
    @XmlAttribute(name = "name")
    private String name;

    @XmlAttribute(name = "time")
    private String time;

    @XmlAttribute(name = "status")
    private String status;

    @XmlAttribute(name = "classname")
    private String className;


    public String getTestName() {
        return name;
    }

    public TestCase setTestName(String name) {
        this.name = name;
        return this;
    }

    public String getTestTime() {
        return time;
    }

    public TestCase setTestTime(String time) {
        this.time = time;
        return this;
    }

    public String getTestStatus() {
        return status;
    }

    public TestCase setTestStatus(String status) {
        this.status = status;
        return this;
    }

    public String getTestClassName() {
        return className;
    }

    public TestCase setTestClassName(String className) {
        this.className = className;
        return this;
    }


}
