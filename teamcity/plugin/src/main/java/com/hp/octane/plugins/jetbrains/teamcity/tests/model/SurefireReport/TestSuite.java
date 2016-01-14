package com.hp.octane.plugins.jetbrains.teamcity.tests.model.SurefireReport;

import javax.xml.bind.annotation.*;
import java.util.List;

/**
 * Created by lev on 12/01/2016.
 */
@XmlRootElement(name = "testsuite")
public class TestSuite {


    private List<TestCase> testCaseList;
    private List<SurefireProperties> surefirePropertiesList;
    private String skipped;
    private String errors;
    private String time;
    private String name;
    private String failures;
    private String tests;


    public List<TestCase> getTestCaseList() {
        return testCaseList;
    }

    @XmlAnyElement
    @XmlElement(name = "testcase")
    public void setTestCaseList(List<TestCase> testCaseList) {
        this.testCaseList = testCaseList;
    }

    public String getSkipped() {
        return skipped;
    }

    @XmlAttribute(name = "skipped")
    public void setSkipped(String skipped) {
        this.skipped = skipped;
    }

    public String getErrors() {
        return errors;
    }

    @XmlAttribute(name = "errors")
    public void setErrors(String errors) {
        this.errors = errors;
    }

    public String getTime() {
        return time;
    }

    @XmlAttribute(name = "time")
    public void setTime(String time) {
        this.time = time;
    }

    public String getName() {
        return name;
    }

    @XmlAttribute(name = "name")
    public void setName(String name) {
        this.name = name;
    }

    public String getFailures() {
        return failures;
    }

    @XmlAttribute(name = "failures")
    public void setFailures(String failures) {
        this.failures = failures;
    }

    public String getTests() {
        return tests;
    }

    @XmlAttribute(name = "tests")
    public void setTests(String tests) {
        this.tests = tests;
    }

    public List<SurefireProperties> getSurefirePropertiesList() {
        return surefirePropertiesList;
    }

    @XmlAnyElement(lax = true)
    @XmlElementWrapper(name = "properties")
    @XmlElement(name = "property")
    public void setSurefirePropertiesList(List<SurefireProperties> surefirePropertiesList) {
        this.surefirePropertiesList = surefirePropertiesList;
    }
}
