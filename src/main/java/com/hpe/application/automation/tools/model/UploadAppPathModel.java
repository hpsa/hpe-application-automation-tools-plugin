package com.hpe.application.automation.tools.model;

import hudson.Extension;
import hudson.model.AbstractDescribableImpl;
import hudson.model.Descriptor;
import org.kohsuke.stapler.DataBoundConstructor;

/**
 * Created with IntelliJ IDEA.
 * User: jingwei
 * Date: 5/20/16
 * Time: 2:52 PM
 * To change this template use File | Settings | File Templates.
 */
public class UploadAppPathModel extends AbstractDescribableImpl<UploadAppPathModel> {
    private String mcAppPath;

    @DataBoundConstructor
    public UploadAppPathModel(String mcAppPath) {
        this.mcAppPath = mcAppPath;
    }

    public String getMcAppPath() {
        return mcAppPath;
    }

    @Extension
    public static class DescriptorImpl extends Descriptor<UploadAppPathModel> {
        public String getDisplayName() {
            return "";
        }
    }
}
