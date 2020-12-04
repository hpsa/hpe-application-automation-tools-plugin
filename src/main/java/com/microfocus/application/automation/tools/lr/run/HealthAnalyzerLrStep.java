/*
 * Certain versions of software and/or documents ("Material") accessible here may contain branding from
 * Hewlett-Packard Company (now HP Inc.) and Hewlett Packard Enterprise Company.  As of September 1, 2017,
 * the Material is now offered by Micro Focus, a separately owned and operated company.  Any reference to the HP
 * and Hewlett Packard Enterprise/HPE marks is historical in nature, and the HP and Hewlett Packard Enterprise/HPE
 * marks are the property of their respective owners.
 * __________________________________________________________________
 * MIT License
 *
 * (c) Copyright 2012-2021 Micro Focus or one of its affiliates.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated
 * documentation files (the "Software"), to deal in the Software without restriction, including without limitation
 * the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software,
 * and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or
 * substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO
 * THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT,
 * TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 *
 * ___________________________________________________________________
 */

package com.microfocus.application.automation.tools.lr.run;

import com.microfocus.application.automation.tools.common.utils.HealthAnalyzerCommon;
import com.microfocus.application.automation.tools.common.utils.OperatingSystem;
import com.microfocus.application.automation.tools.common.model.HealthAnalyzerModel;
import com.microfocus.application.automation.tools.common.model.VariableWrapper;
import com.microfocus.application.automation.tools.common.model.VariableListWrapper;
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
    private static final transient HealthAnalyzerCommon healthAnalyzerCommon =
            new HealthAnalyzerCommon(Messages.ProductName());
    private final boolean checkLrInstallation;
    private final boolean checkOsVersion;
    private final VariableListWrapper checkFiles;


    @DataBoundConstructor
    public HealthAnalyzerLrStep(boolean checkLrInstallation, boolean checkOsVersion, VariableListWrapper checkFiles) {
        this.checkLrInstallation = checkLrInstallation;
        this.checkOsVersion = checkOsVersion;
        this.checkFiles = checkFiles;
    }

    public boolean isCheckOsVersion() {
        return checkOsVersion;
    }

    public VariableListWrapper getCheckFiles() {
        return checkFiles;
    }

    public boolean isFilesExist() {
        return checkFiles != null;
    }

    public List<VariableWrapper> getFilesList() {
        return checkFiles != null ? checkFiles.getFilesList() : null;
    }

    public boolean isCheckLrInstallation() {
        return checkLrInstallation;
    }

    @Override
    public void perform(@Nonnull Run<?, ?> run, @Nonnull FilePath workspace, @Nonnull Launcher launcher,
                        @Nonnull TaskListener listener) throws InterruptedException, IOException {
        healthAnalyzerCommon.ifCheckedPerformWindowsInstallationCheck(LR_REGISTRY_PATH, checkLrInstallation, workspace);
        healthAnalyzerCommon.ifCheckedPerformFilesExistenceCheck(getFilesList(), isFilesExist(), workspace);
        healthAnalyzerCommon.ifCheckedPerformOsCheck(OperatingSystem.WINDOWS, checkOsVersion, workspace);
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
