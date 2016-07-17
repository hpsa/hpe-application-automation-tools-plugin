package com.hp.octane.integrations.dto.tests.impl;

import com.hp.octane.integrations.dto.tests.Property;
import com.hp.octane.integrations.dto.tests.TestCase;
import com.hp.octane.integrations.dto.tests.TestSuite;

import javax.xml.bind.annotation.*;
import java.util.List;

/**
 * Created by lev on 31/05/2016.
 */
@XmlRootElement(name = "testsuite")
@XmlAccessorType(XmlAccessType.NONE)
public class TestSuiteImpl implements TestSuite {

    @XmlElementWrapper(name = "properties")
    @XmlAnyElement(lax = true)
    List<Property> properties;

    @XmlAnyElement(lax = true)
    List<TestCase> testCases;

    public List<Property> getProprties() {
        return properties;
    }

    public TestSuite setProperties(List<Property> properties) {
        this.properties = properties;
        return this;
    }

    public List<TestCase> getTestCases() {
        return testCases;
    }

    public TestSuite setTestCases(List<TestCase> testCases) {
        this.testCases = testCases;
        return this;
    }
}
