package com.hp.octane.integrations.dto.impl.tests;

import com.hp.octane.integrations.dto.api.tests.Property;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Created by lev on 31/05/2016.
 */
@XmlRootElement(name = "property")
@XmlAccessorType(XmlAccessType.NONE)
public class PropertyImpl implements Property {

    @XmlAttribute(name = "name")
    private String propertyName;

    @XmlAttribute(name = "value")
    private String propertyValue;

    public String getPropertyName() {
        return propertyName;
    }

    public Property setPropertyName(String name) {
        propertyName = name;
        return this;
    }

    public String getPropertyValue() {
        return propertyValue;
    }

    public Property setPropertyValue(String value) {
        propertyValue = value;
        return this;
    }
}
