/*
 * © Copyright 2013 EntIT Software LLC
 *  Certain versions of software and/or documents (“Material”) accessible here may contain branding from
 *  Hewlett-Packard Company (now HP Inc.) and Hewlett Packard Enterprise Company.  As of September 1, 2017,
 *  the Material is now offered by Micro Focus, a separately owned and operated company.  Any reference to the HP
 *  and Hewlett Packard Enterprise/HPE marks is historical in nature, and the HP and Hewlett Packard Enterprise/HPE
 *  marks are the property of their respective owners.
 * __________________________________________________________________
 * MIT License
 *
 * Copyright (c) 2018 Micro Focus Company, L.P.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 * ___________________________________________________________________
 *
 */

package com.hpe.application.automation.tools.settings;

import com.hpe.application.automation.tools.model.SrfServerSettingsModel;
import hudson.CopyOnWrite;
import hudson.Extension;
import hudson.model.AbstractProject;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.Builder;
import hudson.util.FormValidation;
import net.sf.json.JSONObject;
import org.apache.commons.lang.StringUtils;
import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.StaplerRequest;

/**
 * Created by shepshel on 20/07/2016.
 */
public class SrfServerSettingsBuilder extends Builder{

        public SrfServerSettingsBuilder(){

        }

        @Override
        public SrfServerSettingsBuilder.SrfDescriptorImpl getDescriptor() {
            return (SrfServerSettingsBuilder.SrfDescriptorImpl) super.getDescriptor();
        }

        /**
         * Descriptor for {@link SrfServerSettingsBuilder}. Used as a singleton. The class is marked as
         * public so that it can be accessed from views.
         *
         * <p>
         * See <tt>src/main/resources/hudson/plugins/hello_world/HelloWorldBuilder/*.jelly</tt> for the
         * actual HTML fragment for the configuration screen.
         */
        @Extension
        // This indicates to Jenkins that this is an implementation of an extension
        // point.
        public static final class SrfDescriptorImpl extends BuildStepDescriptor<Builder> {
            private static String srfServer;
            private int lastId = 0;
            private Object _addButton;
            public Object getaddButton(){
                return _addButton;
            }
            public void setaddButton(Object btn){
                _addButton = btn;
            }
            public boolean getAddButtonState(SrfDescriptorImpl o){
                boolean b= true;
                if(o == null)
                    return false;
                if(o.installations == null)
                    return false;
                if(o.installations[0] == null)
                    return false;

                return b;

            }


            @Override
            public boolean isApplicable(
                    @SuppressWarnings("rawtypes") Class<? extends AbstractProject> aClass) {
                // Indicates that this builder can be used with all kinds of project
                // types
                return true;
            }

            /**
             * This human readable name is used in the configuration screen.
             */
            @Override
            public String getDisplayName() {
                return "";
            }

            public SrfDescriptorImpl() {
                load();
            }
            public static String getSrfServerName() {
                return srfServer;
            }
            @Override
            public boolean configure(StaplerRequest req, JSONObject formData) throws FormException {
                // To persist global configuration information,
                // set that to properties and call save().
                // useFrench = formData.getBoolean("useFrench");
                // ^Can also use req.bindJSON(this, formData);
                // (easier when there are many fields; need set* methods for this,
                // like setUseFrench)
                // req.bindParameters(this, "locks.");

                setInstallations(req.bindParametersToList(SrfServerSettingsModel.class, "srf.").toArray(
                        new SrfServerSettingsModel[0]));

                save();
                return super.configure(req, formData);
            }



            @CopyOnWrite
            private SrfServerSettingsModel[] installations = new SrfServerSettingsModel[0];

            public SrfServerSettingsModel[] getInstallations() {
                return installations;
            }

            public void setInstallations(SrfServerSettingsModel... installations)  {
                this.installations = installations;

                UpdateButtonState();
            }
            private void UpdateButtonState(){
                try {
                    if(hasSrfServers())
                        _addButton.getClass().getField("disabled").setBoolean(_addButton, true);
                    else
                        _addButton.getClass().getField("disabled").setBoolean(_addButton, false);
                }
                catch (Exception e) {
                    e.printStackTrace();
                }
            }
            public FormValidation doCheckSrfServerName(@QueryParameter String value) {
                UpdateButtonState();
                if (StringUtils.isBlank(value)) {
                    return FormValidation.error("Srf server name cannot be empty");
                }

                return FormValidation.ok();
            }



            public Boolean hasSrfServers() {
                return installations.length > 0;
            }
        }
}
