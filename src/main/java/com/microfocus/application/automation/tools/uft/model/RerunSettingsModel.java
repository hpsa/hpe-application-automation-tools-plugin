/*
 *
 *  Certain versions of software and/or documents (“Material”) accessible here may contain branding from
 *  Hewlett-Packard Company (now HP Inc.) and Hewlett Packard Enterprise Company.  As of September 1, 2017,
 *  the Material is now offered by Micro Focus, a separately owned and operated company.  Any reference to the HP
 *  and Hewlett Packard Enterprise/HPE marks is historical in nature, and the HP and Hewlett Packard Enterprise/HPE
 *  marks are the property of their respective owners.
 * __________________________________________________________________
 * MIT License
 *
 * © Copyright 2012-2018 Micro Focus or one of its affiliates.
 *
 * The only warranties for products and services of Micro Focus and its affiliates
 * and licensors (“Micro Focus”) are set forth in the express warranty statements
 * accompanying such products and services. Nothing herein should be construed as
 * constituting an additional warranty. Micro Focus shall not be liable for technical
 * or editorial errors or omissions contained herein.
 * The information contained herein is subject to change without notice.
 * ___________________________________________________________________
 *
 */

package com.microfocus.application.automation.tools.uft.model;

import com.microfocus.application.automation.tools.uft.utils.UftToolUtils;
import hudson.Extension;
import hudson.model.AbstractDescribableImpl;
import hudson.model.Descriptor;
import hudson.util.FormValidation;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;
import org.kohsuke.stapler.QueryParameter;

import javax.annotation.Nonnull;

public class RerunSettingsModel extends AbstractDescribableImpl<RerunSettingsModel> {
    private String test;
    private Boolean checked;
    private Integer numberOfReruns;
    private String cleanupTest;

    @DataBoundConstructor
    public RerunSettingsModel(String test, Boolean checked, Integer numberOfReruns, String cleanupTest) {
        this.test = test;
        this.checked = checked;
        this.numberOfReruns = numberOfReruns;
        this.cleanupTest = cleanupTest;
    }

    public String getTest() {
        return test;
    }

    @DataBoundSetter
    public void setTest(String test) {
        this.test = test;
    }

    public Boolean getChecked() {
        return checked;
    }

    @DataBoundSetter
    public void setChecked(Boolean checked) {
        this.checked = checked;
    }

    public Integer getNumberOfReruns() {
        return numberOfReruns;
    }

    @DataBoundSetter
    public void setNumberOfReruns(Integer numberOfReruns) {
        this.numberOfReruns = numberOfReruns;
    }

    public String getCleanupTest() {
        return cleanupTest;
    }

    @DataBoundSetter
    public void setCleanupTest(String cleanupTest) {
        this.cleanupTest = cleanupTest;
    }

    @Extension
    public static class DescriptorImpl extends Descriptor<RerunSettingsModel> {
        @Nonnull
        @Override
        public String getDisplayName() {
            return "Rerun settings model";
        }

        public FormValidation doCheckNumberOfReruns(@QueryParameter String value) {
            return UftToolUtils.doCheckNumberOfReruns(value);
        }
    }
}
