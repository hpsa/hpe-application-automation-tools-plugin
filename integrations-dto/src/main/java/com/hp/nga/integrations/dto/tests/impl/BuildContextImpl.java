package com.hp.nga.integrations.dto.tests.impl;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import  com.hp.nga.integrations.dto.tests.BuildContext;


/**
 * Created by lev on 06/03/2016.
 */
@JacksonXmlRootElement(localName = "build")
public class BuildContextImpl implements BuildContext {

    private long buildId;

    private String subType;

    private String buildType;

    private String server;

    @JacksonXmlProperty(isAttribute = true, localName = "build_sid")
    public long getBuildId() {
        return buildId;
    }

    public BuildContext setBuildId(long buildId) {
        this.buildId = buildId;
        return this;
    }

    @JacksonXmlProperty(isAttribute = true, localName = "sub_type")
    public String getSubType() {
        return subType;
    }

    public BuildContext setSubType(String subType) {
        this.subType = subType;
        return this;
    }

    @JacksonXmlProperty(isAttribute = true, localName = "build_type")
    public String getBuildType() {
        return buildType;
    }

    public BuildContext setBuildType(String buildType) {
        this.buildType = buildType;
        return this;
    }

    @JacksonXmlProperty(isAttribute = true, localName = "server")
    public String getServer() {
        return server;
    }

    public BuildContext setServer(String server) {
        this.server = server;
        return this;
    }


}
