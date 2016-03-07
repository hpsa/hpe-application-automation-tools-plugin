package com.hp.octane.plugins.jetbrains.teamcity.tests.model;



import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;
import java.util.List;

@XmlRootElement(name = "test_result")
public class TestResultContainer  implements Serializable {
    @XmlElement(name = "build",nillable=true, required=true)
    private BuildContext build;


    @XmlElement(name = "test_run", nillable=true, required=true)
    @XmlElementWrapper(name = "test_runs")
    private List<TestResult> testRuns;




    public TestResultContainer(List<TestResult> testRuns, BuildContext build){
        this.testRuns = testRuns;
        this.build = build;

    }

    public List<TestResult> getTestRuns() {
        return testRuns;
    }

    public BuildContext getBuild() {
        return build;
    }


    //    public ResultFields getResultFields() {
//        return resultFields;
//    }
}
