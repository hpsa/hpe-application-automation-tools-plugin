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

package com.microfocus.application.automation.tools.uft.model;

import hudson.Extension;
import hudson.model.AbstractDescribableImpl;
import hudson.model.Descriptor;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;

import javax.annotation.Nonnull;
import java.util.Properties;

/**
 * Contains the model for the filtering options in case of a failed build, when running the tests through ALM
 */
public class FilterTestsModel extends AbstractDescribableImpl<FilterTestsModel> {
    private String testName;
    private Boolean blockedCheckbox;
    private Boolean failedCheckbox;
    private Boolean notCompletedCheckbox;
    private Boolean noRunCheckbox;
    private Boolean passedCheckbox;

    @DataBoundConstructor
    public FilterTestsModel(String testName, Boolean blockedCheckbox, Boolean failedCheckbox,
                            Boolean notCompletedCheckbox, Boolean noRunCheckbox, Boolean passedCheckbox) {
        this.testName = testName;
        this.blockedCheckbox = blockedCheckbox;
        this.failedCheckbox = failedCheckbox;
        this.notCompletedCheckbox = notCompletedCheckbox;
        this.noRunCheckbox = noRunCheckbox;
        this.passedCheckbox = passedCheckbox;
    }

    public String getTestName() {
        return testName;
    }

    @DataBoundSetter
    public void setTestName(String testName) {
        this.testName = testName;
    }

    public Boolean getBlockedCheckbox() {
        return blockedCheckbox;
    }

    public void setBlockedCheckbox(Boolean blockedCheckbox) {
        this.blockedCheckbox = blockedCheckbox;
    }

    public Boolean getFailedCheckbox() {
        return failedCheckbox;
    }

    public void setFailedCheckbox(Boolean failedCheckbox) {
        this.failedCheckbox = failedCheckbox;
    }

    public Boolean getNotCompletedCheckbox() {
        return notCompletedCheckbox;
    }

    public void setNotCompletedCheckbox(Boolean notCompletedCheckbox) {
        this.notCompletedCheckbox = notCompletedCheckbox;
    }

    public Boolean getNoRunCheckbox() {
        return noRunCheckbox;
    }

    public void setNoRunCheckbox(Boolean noRunCheckbox) {
        this.noRunCheckbox = noRunCheckbox;
    }

    public Boolean getPassedCheckbox() {
        return passedCheckbox;
    }

    public void setPassedCheckbox(Boolean passedCheckbox) {
        this.passedCheckbox = passedCheckbox;
    }

    public void addProperties(Properties props) {
        props.put("FilterTests", "true");
        props.put("FilterByName", this.testName);

        StringBuilder statusList = new StringBuilder();
        addStatus(blockedCheckbox,"Blocked", statusList);
        addStatus(failedCheckbox,"Failed", statusList);
        addStatus(notCompletedCheckbox,"\"Not Completed\"", statusList);
        addStatus(noRunCheckbox,"\"No Run\"", statusList);
        addStatus(passedCheckbox,"Passed", statusList);

        if(statusList.length() > 0){
            statusList.replace(statusList.lastIndexOf(","), statusList.lastIndexOf(" "),"");
        }

        props.put("FilterByStatus", statusList.toString());
    }

    public void addStatus(boolean status,  String statusName, StringBuilder statusList){
        if(status){
            statusList.append(statusName);
            statusList.append(", ");
        }
    }

    @Extension
    public static class DescriptorImpl extends Descriptor<FilterTestsModel> {
        @Nonnull
        public String getDisplayName() {return "Filter tests model";}
    }
}
