// (c) Copyright 2016 Hewlett Packard Enterprise Development LP
// Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
// The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
// THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.

package com.hp.application.automation.tools.model;

import hudson.Extension;
import hudson.model.AbstractDescribableImpl;
import hudson.model.Descriptor;
import hudson.util.FormValidation;
import hudson.util.Secret;
import org.apache.commons.lang.StringUtils;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.QueryParameter;

public class SvServiceSelectionModel extends AbstractDescribableImpl<SvServiceSelectionModel> {

    protected final Kind serviceSelectionKind;
    protected final String service;
    protected final String projectPath;
    protected final Secret projectPassword;

    @DataBoundConstructor
    public SvServiceSelectionModel(Kind serviceSelectionKind, String service, String projectPath, Secret projectPassword) {
        this.serviceSelectionKind = serviceSelectionKind;
        this.service = service;
        this.projectPath = projectPath;
        this.projectPassword = projectPassword;
    }

    public Kind getServiceSelectionKind() {
        return serviceSelectionKind;
    }

    public String getService() {
        return (StringUtils.isNotBlank(service)) ? service : null;
    }

    public String getProjectPath() {
        return (StringUtils.isNotBlank(projectPath)) ? projectPath : null;
    }

    public String getProjectPassword() {
        return (projectPassword != null) ? projectPassword.getPlainText() : null;
    }

    @SuppressWarnings("unused")
    public boolean isSelectedServiceSelectionKind(String kind) {
        return Kind.valueOf(kind) == serviceSelectionKind;
    }

    public enum Kind {
        SERVICE,
        PROJECT,
        ALL_DEPLOYED,
        DEPLOY
    }

    @Extension
    public static class DescriptorImpl extends Descriptor<SvServiceSelectionModel> {

        public String getDisplayName() {
            return "Service Selection";
        }

        @SuppressWarnings("unused")
        public FormValidation doCheckService(@QueryParameter String value) {
            if (StringUtils.isBlank(value)) {
                return FormValidation.error("Service name or id must be set");
            }
            return FormValidation.ok();
        }

        @SuppressWarnings("unused")
        public FormValidation doCheckProjectPath(@QueryParameter String value) {
            if (StringUtils.isBlank(value)) {
                return FormValidation.error("Project path cannot be empty");
            }
            return FormValidation.ok();
        }

    }
}
