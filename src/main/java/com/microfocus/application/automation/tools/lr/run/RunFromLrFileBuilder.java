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

import com.microfocus.application.automation.tools.model.FileSystemTestSetModel;
import com.microfocus.application.automation.tools.model.RunFromFileSystemModel;
import hudson.util.FormValidation;
import org.apache.commons.lang.StringUtils;
import org.kohsuke.stapler.QueryParameter;

/**
 * Describs a regular jenkins build step from UFT or LR
 */
public class RunFromLrFileBuilder {
    /**
     * Do check fs timeout form validation.
     *
     * @param value the value
     * @return the form validation
     */
    public FormValidation doCheckFsTimeout(@QueryParameter String value) {
        if (StringUtils.isEmpty(value)) {
            return FormValidation.ok();
        }

        String sanitizedValue = value.trim();
        if (sanitizedValue.length() > 0 && sanitizedValue.charAt(0) == '-') {
            sanitizedValue = sanitizedValue.substring(1);
        }

        if (!isParameterizedValue(sanitizedValue) && !StringUtils.isNumeric(sanitizedValue)) {
            return FormValidation.error("Timeout must be a parameter or a number, e.g.: 23, $Timeout or ${Timeout}.");
        }

        return FormValidation.ok();
    }

    /**
     * Check if the value is parameterized.
     *
     * @param value the value
     * @return boolean
     */
    public static boolean isParameterizedValue(String value) {
        //Parameter (with or without brackets)
        return value.matches("^\\$\\{[\\w-. ]*}$|^\\$[\\w-.]*$");
    }

    /**
     * Do check controller polling interval form validation.
     *
     * @param value the value
     * @return the form validation
     */
    public static FormValidation doCheckControllerPollingInterval(@QueryParameter String value) {
        if (StringUtils.isEmpty(value)) {
            return FormValidation.ok();
        }

        if (!StringUtils.isNumeric(value)) {
            return FormValidation.error("Controller Polling Interval must be a number");
        }

        return FormValidation.ok();
    }

    /**
     * Do check per scenario time out form validation.
     *
     * @param value the value
     * @return the form validation
     */
    public static FormValidation doCheckPerScenarioTimeOut(@QueryParameter String value) {
        if (StringUtils.isEmpty(value)) {
            return FormValidation.ok();
        }

        if (!isParameterizedValue(value) && !StringUtils.isNumeric(value)) {
            return FormValidation.error("Per Scenario Timeout must be a parameter or a number, e.g.: 23, $ScenarioDuration or ${ScenarioDuration}.");
        }

        return FormValidation.ok();
    }
}
