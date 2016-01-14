package com.hp.octane.plugins.jetbrains.teamcity.tests.model.SurefireReport;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlValue;

/**
 * Created by lev on 12/01/2016.
 */
@XmlRootElement(name ="failure")
public class TestFailure {
    private String errorType;
    private String systemOut;
    private String message;
    private String description;

    public String getErrorType() {
        return errorType;
    }

    @XmlAttribute(name = "type")
    public void setErrorType(String errorType) {
        this.errorType = errorType;
    }

    public String getSystemOut() {
        return systemOut;
    }

    @XmlElement(name = "system-out")
    public void setSystemOut(String systemOut) {
        this.systemOut = systemOut;
    }

    public String getMessage() {
        return message;
    }

    @XmlAttribute(name = "message")
    public void setMessage(String message) {
        this.message = message;
    }

    public String getDescription() {
        return description;
    }

    @XmlValue
    public void setDescription(String description) {
        this.description = description;
    }
}
