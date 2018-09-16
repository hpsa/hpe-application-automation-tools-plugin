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

package com.microfocus.application.automation.tools.lr.run;

import com.microfocus.application.automation.tools.common.HealthAnalyzerCommon;
import com.microfocus.application.automation.tools.common.model.HealthAnalyzerModel;
import com.microfocus.application.automation.tools.common.model.RepeatableField;
import com.microfocus.application.automation.tools.lr.Messages;
import hudson.Extension;
import hudson.FilePath;
import hudson.Launcher;
import hudson.model.Run;
import hudson.model.TaskListener;
import org.kohsuke.stapler.DataBoundConstructor;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.util.List;

public class HealthAnalyzerLrStep extends HealthAnalyzerModel {
    private static final String LR_REGISTRY_PATH =
            "HKEY_LOCAL_MACHINE\\SOFTWARE\\Wow6432Node\\Mercury Interactive\\LoadRunner\\CurrentVersion";
    private final boolean checkLrInstallation;
    private final List<RepeatableField> filesList;
    private final boolean checkFileExistence;
    private final transient HealthAnalyzerCommon healthAnalyzerCommon = new HealthAnalyzerCommon(Messages.ProductName());

    @DataBoundConstructor
    public HealthAnalyzerLrStep(boolean checkLrInstallation, List<RepeatableField> filesList, boolean checkFileExistence) {
        this.checkLrInstallation = checkLrInstallation;
        this.filesList = filesList;
        this.checkFileExistence = checkFileExistence;
    }

    public boolean isCheckFileExistence() {
        return checkFileExistence;
    }

    public boolean isCheckLrInstallation() {
        return checkLrInstallation;
    }

    public List<RepeatableField> getFilesList() {
        return filesList;
    }

    @Override
    public void perform(@Nonnull Run<?, ?> run, @Nonnull FilePath workspace, @Nonnull Launcher launcher,
                        @Nonnull TaskListener listener) throws InterruptedException, IOException {
        // TODO: Should I check for the exceptions that comes from the ifCheckedPerform..?
        healthAnalyzerCommon.ifCheckedPerformWindowsInstallationCheck(LR_REGISTRY_PATH, checkLrInstallation);
        healthAnalyzerCommon.ifCheckedPerformFilesExistenceCheck(filesList, checkFileExistence);
    }

    @Extension
    public static class DescriptorImpl extends HealthAnalyzerModelDescriptor {
        @Override
        public String toString() {
            return "Info in DescriptorImpl at HealthAnalyzerLrStep";
        }

        @Nonnull
        @Override
        public String getDisplayName() {
            return Messages.ProductName();
        }
    }
}
