/**
 © Copyright 2015 Hewlett Packard Enterprise Development LP

 Permission is hereby granted, free of charge, to any person obtaining a copy
 of this software and associated documentation files (the "Software"), to deal
 in the Software without restriction, including without limitation the rights
 to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 copies of the Software, and to permit persons to whom the Software is
 furnished to do so, subject to the following conditions:

 The above copyright notice and this permission notice shall be included in
 all copies or substantial portions of the Software.

 THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 FITNESS FOR A PARTICULAR PURPOSE AND NON INFRINGEMENT. IN NO EVENT SHALL THE
 AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 THE SOFTWARE.
 */
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
