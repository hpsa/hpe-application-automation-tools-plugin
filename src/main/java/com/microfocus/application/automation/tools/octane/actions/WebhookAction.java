/*
 * Certain versions of software and/or documents ("Material") accessible here may contain branding from
 * Hewlett-Packard Company (now HP Inc.) and Hewlett Packard Enterprise Company.  As of September 1, 2017,
 * the Material is now offered by Micro Focus, a separately owned and operated company.  Any reference to the HP
 * and Hewlett Packard Enterprise/HPE marks is historical in nature, and the HP and Hewlett Packard Enterprise/HPE
 * marks are the property of their respective owners.
 * __________________________________________________________________
 * MIT License
 *
 * (c) Copyright 2012-2019 Micro Focus or one of its affiliates.
 *
 * The only warranties for products and services of Micro Focus and its affiliates
 * and licensors ("Micro Focus") are set forth in the express warranty statements
 * accompanying such products and services. Nothing herein should be construed as
 * constituting an additional warranty. Micro Focus shall not be liable for technical
 * or editorial errors or omissions contained herein.
 * The information contained herein is subject to change without notice.
 * ___________________________________________________________________
 */

package com.microfocus.application.automation.tools.octane.actions;

import com.microfocus.application.automation.tools.octane.model.SonarHelper;
import hudson.model.Action;

import javax.annotation.CheckForNull;
import java.util.List;

/*
    Class for handling webhook exception
 */

public class WebhookAction implements Action {
    private Boolean isExpectingToGetWebhookCall;
    private String serverUrl;
    private List<SonarHelper.DataType> dataTypeList;


    public WebhookAction(Boolean isExpectingToGetWebhookCall, String serverUrl, List<SonarHelper.DataType> dataTypeList) {
        this.isExpectingToGetWebhookCall = isExpectingToGetWebhookCall;
        this.serverUrl = serverUrl;
        this.dataTypeList = dataTypeList;
    }


    public String getServerUrl() {

        return serverUrl;
    }

    public Boolean getExpectingToGetWebhookCall() {
        return isExpectingToGetWebhookCall;
    }

    public List<SonarHelper.DataType> getDataTypeList() {
        return dataTypeList;
    }

    @CheckForNull
    @Override
    public String getIconFileName() {
        return null;
    }

    @CheckForNull
    @Override
    public String getDisplayName() {
        return null;
    }

    @CheckForNull
    @Override
    public String getUrlName() {
        return null;
    }
}