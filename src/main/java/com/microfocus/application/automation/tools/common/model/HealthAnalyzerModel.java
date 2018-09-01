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

package com.microfocus.application.automation.tools.common.model;

import hudson.*;
import hudson.model.Describable;
import hudson.model.Descriptor;
import hudson.model.Run;
import hudson.model.TaskListener;
import jenkins.model.Jenkins;

import javax.annotation.Nonnull;
import java.io.IOException;

public abstract class HealthAnalyzerModel implements ExtensionPoint, Describable<HealthAnalyzerModel> {

    public HealthAnalyzerModelDescriptor getDescriptor() {
        return (HealthAnalyzerModelDescriptor) Jenkins.getInstance().getDescriptor(getClass());
    }

    public static ExtensionList<HealthAnalyzerModel> all() {
        return Jenkins.getInstance().getExtensionList(HealthAnalyzerModel.class);
    }

    public abstract void perform(
            @Nonnull Run<?, ?> run, @Nonnull FilePath workspace,
            @Nonnull Launcher launcher, @Nonnull TaskListener listener)
            throws InterruptedException, IOException;

    public abstract static class HealthAnalyzerModelDescriptor extends Descriptor<HealthAnalyzerModel> {
        protected HealthAnalyzerModelDescriptor() {
        }

        @Override
        public String toString() {
            return "Info from HealthAnalyzerModelDescriptor";
        }

        public static DescriptorExtensionList<HealthAnalyzerModel,HealthAnalyzerModelDescriptor> all() {
            return Jenkins.getInstance().getDescriptorList(HealthAnalyzerModel.class);
        }
    }
}
