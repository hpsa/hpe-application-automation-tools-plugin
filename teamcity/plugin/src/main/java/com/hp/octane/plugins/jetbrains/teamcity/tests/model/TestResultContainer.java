package com.hp.octane.plugins.jetbrains.teamcity.tests.model;



import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;

@XmlRootElement(name = "test_result")
public class TestResultContainer {

    @XmlElement(name = "test_run")
    @XmlElementWrapper(name = "test_runs")
    private List<TestResult> testRuns;
    //private ResultFields resultFields;

    public TestResultContainer(List<TestResult> testRuns){//, ResultFields resultFields) {
        this.testRuns = testRuns;
        //this.resultFields = resultFields;
    }

    public List<TestResult> getTestRuns() {
        return testRuns;
    }

//    public ResultFields getResultFields() {
//        return resultFields;
//    }
}
