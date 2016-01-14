package com.hp.octane.plugins.jetbrains.teamcity.tests.model.SurefireReport;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Created by lev on 12/01/2016.
 */
@XmlRootElement(name = "property")
public class SurefireProperties {

    private String name;
    private String value;

    public String getName() {
        return name;
    }

    @XmlAttribute(name = "name")
    public void setName(String name) {
        this.name = name;
    }

    public String getValue() {
        return value;
    }

    @XmlAttribute(name = "value")
    public void setValue(String value) {
        this.value = value;
    }
}
