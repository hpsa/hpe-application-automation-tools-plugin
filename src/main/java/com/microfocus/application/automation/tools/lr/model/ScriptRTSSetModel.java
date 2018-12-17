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
import org.apache.commons.lang.StringUtils;
import javax.annotation.Nonnull;
import java.util.List;
import java.util.Properties;

/**
 * Use case: users can add runtime settings to scripts from jenkins (currently only additional
 * attributes)
 * This model will be sent to HpToolsLauncher (by saving it in the props.txt file) which parses
 * the scripts and performs api calls on controller
 *
 * Describes a container for scripts and their associated runtime settings
 */
public class ScriptRTSSetModel extends AbstractDescribableImpl<ScriptRTSSetModel> {
    private List<ScriptRTSModel> scripts;

    @DataBoundConstructor
    public ScriptRTSSetModel(List<ScriptRTSModel> scripts) {
        this.scripts = scripts;
    }

    public List<ScriptRTSModel> getScripts() {
        return scripts;
    }

    /**
     * Adds scripts to the props file containing script name
     *
     * @param props
     */
    public void addScriptsToProps(Properties props) {
        int scriptCounter = 1;

        ScriptRTSModel.additionalAttributeCounter = 1;
        for (ScriptRTSModel script: this.scripts) {
            if (!StringUtils.isEmpty(script.getScriptName())) {
                props.put("ScriptRTS" + scriptCounter, script.getScriptName());
                script.addAdditionalAttributesToPropsFile(props, script.getScriptName());
                scriptCounter++;
            }
        }
    }

    @Extension
    public static class DescriptorImpl extends Descriptor<ScriptRTSSetModel>
    {
        @Nonnull
        public String getDisplayName() { return Messages.ScriptRTSSetModel(); }
    }
}
