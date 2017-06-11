package com.hpe.application.automation.tools.model;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * @author Effi Bar-She'an
 */
@XmlRootElement(name = "SiteVersions")
public class ALMVersion {

    @XmlElement(name = "MajorVersion")
    private String _majorVersion;
    @XmlElement(name = "MinorVersion")
    private String _minorVersion;

    public String getMajorVersion() {

        return _majorVersion;
    }

    public void setMajorVersion(String majorVersion) {

        _majorVersion = majorVersion;
    }

    public String getMinorVersion() {

        return _minorVersion;
    }

    public void setMinorVersion(String minorVersion) {

        _minorVersion = minorVersion;
    }
}
