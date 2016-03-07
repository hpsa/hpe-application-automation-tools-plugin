package com.hp.octane.plugins.jetbrains.teamcity.tests.model;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import java.io.Serializable;

/**
 * Created by lev on 06/03/2016.
 */
@XmlAccessorType(XmlAccessType.FIELD)
public class BuildContext {
    @XmlAttribute(name = "build_sid")
    private long buildId;

    @XmlAttribute(name = "sub_type")
    private String subType;

    @XmlAttribute(name = "build_type")
    private String buildType;

    @XmlAttribute(name = "server")
    private String server;


    public long getBuildId() {
        return buildId;
    }

    public void setBuildId(long buildId) {
        this.buildId = buildId;
    }

    public String getSubType() {
        return subType;
    }

    public void setSubType(String subType) {
        this.subType = subType;
    }

    public String getBuildType() {
        return buildType;
    }

    public void setBuildType(String buildType) {
        this.buildType = buildType;
    }

    public String getServer() {
        return server;
    }

    public void setServer(String server) {
        this.server = server;
    }
}
