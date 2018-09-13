/*
 * Certain versions of software and/or documents ("Material") accessible here may contain branding from
 * Hewlett-Packard Company (now HP Inc.) and Hewlett Packard Enterprise Company.  As of September 1, 2017,
 * the Material is now offered by Micro Focus, a separately owned and operated company.  Any reference to the HP
 * and Hewlett Packard Enterprise/HPE marks is historical in nature, and the HP and Hewlett Packard Enterprise/HPE
 * marks are the property of their respective owners.
 * __________________________________________________________________
 * MIT License
 *
 * (c) Copyright 2012-2018 Micro Focus or one of its affiliates.
 *
 * The only warranties for products and services of Micro Focus and its affiliates
 * and licensors ("Micro Focus") are set forth in the express warranty statements
 * accompanying such products and services. Nothing herein should be construed as
 * constituting an additional warranty. Micro Focus shall not be liable for technical
 * or editorial errors or omissions contained herein.
 * The information contained herein is subject to change without notice.
 * ___________________________________________________________________
 */

package com.microfocus.application.automation.tools.mc.run;

import com.microfocus.application.automation.tools.common.HealthAnalyzerCommon;
import com.microfocus.application.automation.tools.common.model.HealthAnalyzerModel;
import com.microfocus.application.automation.tools.mc.Messages;
import com.microfocus.application.automation.tools.model.MCServerSettingsModel;
import com.microfocus.application.automation.tools.settings.MCServerSettingsBuilder;
import hudson.Extension;
import hudson.FilePath;
import hudson.Launcher;
import hudson.model.Run;
import hudson.model.TaskListener;
import jenkins.model.Jenkins;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;

import javax.annotation.Nonnull;
import java.io.IOException;

public class HealthAnalyzerMcStep extends HealthAnalyzerModel {
    private final boolean checkMcServer;
    private final transient HealthAnalyzerCommon healthAnalyzerCommon = new HealthAnalyzerCommon(Messages.ProductName());
    private String mcServerUrl;

    @DataBoundConstructor
    public HealthAnalyzerMcStep(boolean checkMcServer, String mcServerUrl) {
        this.checkMcServer = checkMcServer;
        this.mcServerUrl = mcServerUrl;
    }

    public String getMcServerUrl() {
        return mcServerUrl;
    }

    @DataBoundSetter
    public void setMcServerUrl(String mcServerUrl) {
        this.mcServerUrl = mcServerUrl;
    }

    public boolean isCheckMcServer() {
        return checkMcServer;
    }

    @Override
    public void perform(
            @Nonnull Run<?, ?> run, @Nonnull FilePath workspace, @Nonnull Launcher launcher, @Nonnull TaskListener listener)
            throws InterruptedException, IOException {
        healthAnalyzerCommon.ifCheckedDoesUrlExist(mcServerUrl, checkMcServer);
    }

    @Extension
    public static class DescriptorImpl extends HealthAnalyzerModelDescriptor {
        @Override
        public String toString() {
            return "Inside DescriptorImpl at HealthAnalyzerMcStep";
        }

        @Nonnull
        @Override
        public String getDisplayName() {
            return Messages.ProductName();
        }

        public MCServerSettingsModel[] getMcServers() {
            return Jenkins.getInstance().getDescriptorByType(
                    MCServerSettingsBuilder.MCDescriptorImpl.class).getInstallations();
        }
    }
}
