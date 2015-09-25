package com.hpe.application.automation.bamboo.tasks;

import java.io.Serializable;

/**
 * Created by mprilepina on 14/08/2015.
 */
    public class AlmConfigParameter implements Serializable {

    private String almParamSourceType;
    private String almParamName;
    private String almParamValue;
    private Boolean almParamOnlyFirst;

    public AlmConfigParameter(String almParamSourceType, String almParamName, String almParamValue, String almParamOnlyFirst) {
        this.almParamSourceType = almParamSourceType;
        this.almParamName = almParamName;
        this.almParamValue = almParamValue;
        this.almParamOnlyFirst = Boolean.parseBoolean(almParamOnlyFirst);
    }

    public AlmConfigParameter(AlmConfigParameter configParams) {
        this.almParamSourceType = configParams.getAlmParamSourceType();
        this.almParamName = configParams.getAlmParamName();
        this.almParamValue = configParams.getAlmParamValue();
    }

    public AlmConfigParameter() {
    }

    public String getAlmParamSourceType() {
        return almParamSourceType;
    }

    public void setAlmParamSourceType(String almParamSourceType) {
        this.almParamSourceType = almParamSourceType;
    }

    public String getAlmParamName() {
        return almParamName;
    }

    public void setAlmParamName(String almParamName) {
        this.almParamName = almParamName;
    }

    public String getAlmParamValue() {
        return almParamValue;
    }

    public void setAlmParamValue(String almParamValue) {
        this.almParamValue = almParamValue;
    }

    public Boolean getAlmParamOnlyFirst() {
        return almParamOnlyFirst;
    }

    public void setAlmParamOnlyFirst(Boolean almParamOnlyFirst) {
        this.almParamOnlyFirst = almParamOnlyFirst;
    }
}
