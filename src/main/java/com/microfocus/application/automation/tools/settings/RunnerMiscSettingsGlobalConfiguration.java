/*
 * Certain versions of software and/or documents ("Material") accessible here may contain branding from
 * Hewlett-Packard Company (now HP Inc.) and Hewlett Packard Enterprise Company.  As of September 1, 2017,
 * the Material is now offered by Micro Focus, a separately owned and operated company.  Any reference to the HP
 * and Hewlett Packard Enterprise/HPE marks is historical in nature, and the HP and Hewlett Packard Enterprise/HPE
 * marks are the property of their respective owners.
 * __________________________________________________________________
 * MIT License
 *
 * (c) Copyright 2012-2023 Micro Focus or one of its affiliates.
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

package com.microfocus.application.automation.tools.settings;

import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.Extension;
import hudson.util.FormValidation;
import jenkins.model.GlobalConfiguration;
import com.microfocus.application.automation.tools.sse.common.StringUtils;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.QueryParameter;

import java.io.Serializable;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Extension(ordinal = 1, optional = true)
public class RunnerMiscSettingsGlobalConfiguration extends GlobalConfiguration implements Serializable {

    public static final String DEFAULT_UFT_DATE_PATTERN1 = "dd/MM/yyyy HH:mm:ss";
    // for backward compatibility only, after this it will be forced to use / as date separator
    public static final String DEFAULT_UFT_DATE_PATTERN2 = "dd-MM-yyyy HH:mm:ss";
    public static final String DEFAULT_UFT_DATE_PATTERN3 = "dd.MM.yyyy HH:mm:ss";
    public static final List<String> DEFAULT_UFT_DATE_PATTERNS = Arrays.asList(DEFAULT_UFT_DATE_PATTERN1, DEFAULT_UFT_DATE_PATTERN2, DEFAULT_UFT_DATE_PATTERN3);

    public static final String DEFAULT_BRANCHES = "master main trunk mainline";
    public static final String DEFAULT_OUTPUT_ENVIRONMENT_PARAMETERS = "BUILD_DISPLAY_NAME BUILD_TAG BUILD_URL";

    private String dateFormat;
    private String defaultBranches;
    private String outputEnvironmentParameters;
    private boolean agentToControllerEnabled;

    @DataBoundConstructor
    public RunnerMiscSettingsGlobalConfiguration(String mfDateFormat, String defaultBranches, String outputEnvironmentParameters, boolean agentToControllerEnabled) {
        setDateFormat(mfDateFormat);
        setDefaultBranches(defaultBranches);
        setAgentToControllerEnabled(agentToControllerEnabled);
        setOutputEnvironmentParameters(outputEnvironmentParameters);
    }

    public RunnerMiscSettingsGlobalConfiguration() {
        load();
    }

    public static RunnerMiscSettingsGlobalConfiguration getInstance() throws NullPointerException {
        RunnerMiscSettingsGlobalConfiguration config = GlobalConfiguration.all().get(RunnerMiscSettingsGlobalConfiguration.class);

        if (config == null) throw new NullPointerException();

        return config;
    }

    @NonNull
    @Override
    public String getDisplayName() {
        return "Miscellaneous OpenText Plugin settings";
    }

    public String getDateFormat() {
        return dateFormat;
    }

    public String getDefaultBranches() {
        return defaultBranches;
    }

    public void setDefaultBranches(String defaultBranches) {
        String validatedDefaultBranches = getValidatedDefaultBranches(defaultBranches);
        if (!StringUtils.isNullOrEmpty(validatedDefaultBranches)) {
            this.defaultBranches = validatedDefaultBranches;
        } else {
            this.defaultBranches = DEFAULT_BRANCHES;
        }

        save();
    }

    public String getOutputEnvironmentParameters() {
        return outputEnvironmentParameters != null ? outputEnvironmentParameters : DEFAULT_OUTPUT_ENVIRONMENT_PARAMETERS;
    }

    public void setOutputEnvironmentParameters(String outputEnvironmentParameters) {
        this.outputEnvironmentParameters = outputEnvironmentParameters;
        save();
    }

    private String getValidatedDefaultBranches(String defaultBranches) {
        String[] branches = defaultBranches.split(" ");
        return Stream.of(branches).filter(branch -> !StringUtils.isNullOrEmpty(branch))
                .collect(Collectors.joining(" "));
    }

    public DateTimeFormatter getDateFormatter() {
        return dateFormat != null ? DateTimeFormatter.ofPattern(dateFormat) : DateTimeFormatter.ofPattern(DEFAULT_UFT_DATE_PATTERN1);
    }

    public void setDateFormat(String dateFormat) {
        if (!StringUtils.isNullOrEmpty(dateFormat)) {
            try {
                DateTimeFormatter.ofPattern(dateFormat);
                this.dateFormat = dateFormat;
            } catch (IllegalArgumentException ignored) {
                this.dateFormat = DEFAULT_UFT_DATE_PATTERN1;
            }
        } else {
            this.dateFormat = DEFAULT_UFT_DATE_PATTERN1;
        }

        save();
    }

    public boolean isAgentToControllerEnabled() {
        return agentToControllerEnabled;
    }

    public void setAgentToControllerEnabled(boolean agentToControllerEnabled) {
        this.agentToControllerEnabled = agentToControllerEnabled;
        save();
    }

    public FormValidation doCheckDateFormat(@QueryParameter String value) {
        if (!StringUtils.isNullOrEmpty(value)) {
            try {
                DateTimeFormatter.ofPattern(value);
            } catch (IllegalArgumentException ignored) {
                return FormValidation.error("Invalid timestamp pattern specified.");
            }

            return FormValidation.ok();
        }

        return FormValidation.warning("Will fallback to default pattern.");
    }

    public FormValidation doCheckDefaultBranches(@QueryParameter String value) {
        if (!StringUtils.isNullOrEmpty(value)) {
            return FormValidation.ok();
        }

        return FormValidation.warning("Will fallback to default pattern.");
    }

}
