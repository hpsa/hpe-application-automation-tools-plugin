package com.hp.octane.plugins.jetbrains.teamcity.tests.model.SurefireReport;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import javax.xml.bind.annotation.XmlAnyElement;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Created by lev on 12/01/2016.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@XmlRootElement(name ="testcase")
public class TestCase {
    private String time;
    private String name;
    private String className;
    private TestError error;
    private TestFailure failure;
//    private String message;
//    private String type;


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

    public String getClassName() {
        return className;
    }

    @XmlAttribute(name = "classname")
    public void setClassName(String className) {
        this.className = className;
    }

    public TestError getError() {
        return error;
    }

    @XmlAnyElement
    @XmlElement(name = "error", required = false)
    public void setError(TestError error) {
        this.error = error;
    }

    public TestFailure getFailure() {
        return failure;
    }

    @XmlAnyElement
    @XmlElement(name = "failure" , required = false)
    public void setFailure(TestFailure failure) {
        this.failure = failure;
    }

//    public String getMessage() {
//        return message;
//    }
//
//    @XmlElement(name = "message" , required = false)
//    public void setMessage(String message) {
//        this.message = message;
//    }
//
//    public String getType() {
//        return type;
//    }
//
//    @XmlElement(name = "type" , required = false)
//    public void setType(String type) {
//        this.type = type;
//    }
}
