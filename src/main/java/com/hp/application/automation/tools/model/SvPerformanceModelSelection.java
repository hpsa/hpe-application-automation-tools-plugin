// (c) Copyright 2016 Hewlett Packard Enterprise Development LP
// Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
// The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
// THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.

package com.hp.application.automation.tools.model;

import hudson.Extension;
import hudson.model.AbstractDescribableImpl;
import hudson.model.Descriptor;
import hudson.util.FormValidation;
import org.apache.commons.lang.StringUtils;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.QueryParameter;

public class SvPerformanceModelSelection extends AbstractDescribableImpl<SvPerformanceModelSelection> {

    protected final Kind performanceModelSelectionType;
    protected final String performanceModel;

    @DataBoundConstructor
    public SvPerformanceModelSelection(Kind performanceModelSelectionType, String performanceModel) {
        this.performanceModelSelectionType = performanceModelSelectionType;
        this.performanceModel = performanceModel;
    }

    @SuppressWarnings("unused")
    public Kind getPerformanceModelSelectionType() {
        return performanceModelSelectionType;
    }

    public String getPerformanceModel() {
        return (StringUtils.isNotBlank(performanceModel)) ? performanceModel : null;
    }

    @SuppressWarnings("unused")
    public boolean isSelected(String type) {
        return Kind.valueOf(type).equals(this.performanceModelSelectionType);
    }

    public enum Kind {
        BY_NAME,
        NONE,
        OFFLINE,
        DEFAULT,
    }

    @Extension
    public static class DescriptorImpl extends Descriptor<SvPerformanceModelSelection> {

        public String getDisplayName() {
            return "Performance Model Selection";
        }

        @SuppressWarnings("unused")
        public FormValidation doCheckModel(@QueryParameter String model) {
            if (StringUtils.isBlank(model)) {
                return FormValidation.error("Value cannot be empty");
            }
            return FormValidation.ok();
        }
    }

    @Override
    public String toString() {
        switch (performanceModelSelectionType) {
            case BY_NAME:
                return performanceModel;
            case NONE:
                return "<none>";
            case OFFLINE:
                return "<offline>";
            default:
                return "<default>";
        }
    }

    public String getSelectedModelName() {
        switch (performanceModelSelectionType) {
            case BY_NAME:
                return performanceModel;
            case OFFLINE:
                return "Offline";
            default:
                return null;
        }
    }
}
