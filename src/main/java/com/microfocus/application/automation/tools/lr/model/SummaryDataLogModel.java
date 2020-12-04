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

package com.microfocus.application.automation.tools.lr.model;

import com.microfocus.application.automation.tools.lr.Messages;
import hudson.Extension;
import hudson.util.FormValidation;
import hudson.model.AbstractDescribableImpl;
import hudson.model.Descriptor;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.BooleanUtils;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.QueryParameter;
import javax.annotation.Nonnull;
import java.util.Properties;

public class SummaryDataLogModel extends AbstractDescribableImpl<SummaryDataLogModel> {
    private boolean logVusersStates;
    private boolean logErrorCount;
    private boolean logTransactionStatistics;
    private String pollingInterval;

    @DataBoundConstructor
    public SummaryDataLogModel(boolean logVusersStates, boolean logErrorCount, boolean logTransactionStatistics, String pollingInterval) {
        this.logVusersStates = logVusersStates;
        this.logErrorCount = logErrorCount;
        this.logTransactionStatistics = logTransactionStatistics;
        this.pollingInterval = pollingInterval;
    }

    public boolean getLogVusersStates() {
        return logVusersStates;
    }

    public boolean getLogErrorCount() {
        return logErrorCount;
    }

    public boolean getLogTransactionStatistics() {
        return logTransactionStatistics;
    }

    public String getPollingInterval() { return pollingInterval; }

    public void addToProps(Properties props)
    {
        if (StringUtils.isEmpty(pollingInterval))
        {
            pollingInterval = "30";
        }

        props.put("SummaryDataLog",
                BooleanUtils.toInteger(logVusersStates) + ";"
                + BooleanUtils.toInteger(logErrorCount) + ";"
                + BooleanUtils.toInteger(logTransactionStatistics) + ";"
                + Integer.parseInt(pollingInterval)
        );
    }

    @Extension
    public static class DescriptorImpl extends Descriptor<SummaryDataLogModel> {
        @Nonnull
        public String getDisplayName() { return Messages.SummaryDataLogModel(); }

        public FormValidation doCheckPollingInterval(@QueryParameter String value) {
            if (!StringUtils.isNumeric(value)) {
                return FormValidation.error("Polling Interval must be a number");
            }

            return FormValidation.ok();
        }
    }
}
