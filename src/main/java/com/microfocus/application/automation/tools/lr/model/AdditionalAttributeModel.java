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

package com.microfocus.application.automation.tools.lr.model;

import com.microfocus.application.automation.tools.lr.Messages;
import hudson.Extension;
import hudson.model.AbstractDescribableImpl;
import hudson.model.Descriptor;
import org.kohsuke.stapler.DataBoundConstructor;
import javax.annotation.Nonnull;

/**
 * An additional attribute in the controller runtime's settings consists of: name, value and
 * description (optional)
 *
 */
public class AdditionalAttributeModel extends AbstractDescribableImpl<AdditionalAttributeModel> {
    private String name;
    private String value;
    private String description;

    @DataBoundConstructor
    public AdditionalAttributeModel(String name, String value, String description) {
        this.name = name;
        this.value = value;
        this.description = description;
    }

    public String getName() {
        return name;
    }

    public String getValue() {
        return value;
    }

    public String getDescription() {
        return description;
    }

    @Extension
    public static class DescriptorImpl extends Descriptor<AdditionalAttributeModel>
    {
        @Nonnull
        public String getDisplayName() { return Messages.AdditionalAttributeModel(); }
    }
}
