package com.hp.octane.plugins.jenkins.configuration;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class PredefinedConfiguration {

    private String uiLocation;

    public String getUiLocation() {
        return uiLocation;
    }

    public void setUiLocation(String location) {
        this.uiLocation = location;
    }
}
