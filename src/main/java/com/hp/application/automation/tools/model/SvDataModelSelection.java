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

public class SvDataModelSelection extends AbstractDescribableImpl<SvDataModelSelection> {

    protected final Kind dataModelSelectionType;
    protected final String dataModel;

    @DataBoundConstructor
    public SvDataModelSelection(Kind dataModelSelectionType, String dataModel) {
        this.dataModelSelectionType = dataModelSelectionType;
        this.dataModel = dataModel;
    }

    @SuppressWarnings("unused")
    public Kind getDataModelSelectionType() {
        return dataModelSelectionType;
    }

    public String getDataModel() {
        return (StringUtils.isNotBlank(dataModel)) ? dataModel : null;
    }

    @SuppressWarnings("unused")
    public boolean isSelected(String type) {
        return Kind.valueOf(type).equals(this.dataModelSelectionType);
    }

    public enum Kind {
        BY_NAME,
        NONE,
        DEFAULT,
    }

    @Extension
    public static class DescriptorImpl extends Descriptor<SvDataModelSelection> {

        public String getDisplayName() {
            return "Data model Selection";
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
        switch (dataModelSelectionType) {
            case BY_NAME:
                return dataModel;
            case NONE:
                return "<none>";
            default:
                return "<default>";
        }
    }

    public String getSelectedModelName() {
        switch (dataModelSelectionType) {
            case BY_NAME:
                return dataModel;
            default:
                return null;
        }
    }
}
