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

package com.microfocus.application.automation.tools.uft.model;

import com.microfocus.application.automation.tools.model.EnumDescription;
import com.microfocus.application.automation.tools.uft.utils.UftToolUtils;
import hudson.Extension;
import hudson.model.AbstractDescribableImpl;
import hudson.model.Descriptor;
import hudson.model.Node;
import hudson.util.FormValidation;
import jenkins.model.Jenkins;
import org.apache.commons.lang.StringUtils;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;
import org.kohsuke.stapler.QueryParameter;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

public class UftSettingsModel extends AbstractDescribableImpl<UftSettingsModel> {
    public static final EnumDescription ANY_BUILD_TEST = new EnumDescription("Of any of the build's tests", "Of any " +
            "of the build's tests");
    public static final EnumDescription SPECIFIC_BUILD_TEST = new EnumDescription("Of a specific test in the build",
            "Of a specific test in the build");
    public static final List<EnumDescription> fsTestTypes = Arrays.asList(ANY_BUILD_TEST, SPECIFIC_BUILD_TEST);
    private String selectedNode;
    private String fsTestPath;
    private String numberOfReruns;
    private String cleanupTest;
    private String onCheckFailedTest;
    private String fsTestType;
    private List<RerunSettingsModel> rerunSettingsModels;

    @DataBoundConstructor
    public UftSettingsModel(String selectedNode, String numberOfReruns, String cleanupTest, String onCheckFailedTest,
                            String fsTestType, List<RerunSettingsModel> rerunSettingsModels) {
        this.selectedNode = selectedNode;
        this.numberOfReruns = numberOfReruns;
        this.cleanupTest = cleanupTest;
        this.onCheckFailedTest = onCheckFailedTest;
        this.fsTestType = fsTestType;
        this.setRerunSettingsModels(UftToolUtils.updateRerunSettings(getSelectedNode(), getFsTestPath(),
                rerunSettingsModels));
    }

    public List<String> getNodes() {
        List<Node> nodeList = Jenkins.getInstance().getNodes();
        List<String> nodes = new ArrayList<>();
        nodes.add("master");
        for (Node node : nodeList) {
            nodes.add(node.getDisplayName());
        }

        return nodes;
    }


    public String getSelectedNode() {
        return selectedNode;
    }

    @DataBoundSetter
    public void setSelectedNode(String selectedNode) {
        this.selectedNode = selectedNode;
    }

    public String getFsTestPath() {
        return fsTestPath;
    }

    public void setFsTestPath(String fsTestPath) {
        this.fsTestPath = fsTestPath;
    }

    public String getNumberOfReruns() {
        return numberOfReruns;
    }

    @DataBoundSetter
    public void setNumberOfReruns(String numberOfReruns) {
        this.numberOfReruns = numberOfReruns;
    }

    public String getCleanupTest() {
        return cleanupTest;
    }

    @DataBoundSetter
    public void setCleanupTest(String cleanupTest) {
        this.cleanupTest = cleanupTest;
    }

    public String getOnCheckFailedTest() {
        return onCheckFailedTest;
    }

    @DataBoundSetter
    public void setOnCheckFailedTest(String onCheckFailedTest) {
        this.onCheckFailedTest = onCheckFailedTest;
    }

    public String getFsTestType() {
        return fsTestType;
    }

    @DataBoundSetter
    public void setFsTestType(String fsTestType) {
        this.fsTestType = fsTestType;
    }

    /**
     * Gets the rerun settings
     *
     * @return the rerun settings
     */
    public List<RerunSettingsModel> getRerunSettingsModels() {
        return UftToolUtils.updateRerunSettings(selectedNode, getFsTestPath(), rerunSettingsModels);
    }

    @DataBoundSetter
    public void setRerunSettingsModels(List<RerunSettingsModel> rerunSettingsModels) {
        this.rerunSettingsModels = rerunSettingsModels;
    }

    public List<EnumDescription> getFsTestTypes() {
        return fsTestTypes;
    }

    /**
     * Add properties (failed tests, cleanup tests, number of reruns) to properties file
     *
     * @param props
     */
    public void addToProperties(Properties props) {
        if (!StringUtils.isEmpty(this.selectedNode)) {
            props.put("Selected node", this.selectedNode);
        }

        if (!StringUtils.isEmpty(this.onCheckFailedTest)) {
            props.put("onCheckFailedTest", this.onCheckFailedTest);
        } else {
            props.put("onCheckFailedTest", "");
        }

        props.put("testType", this.fsTestType);

        if (this.fsTestType.equals(fsTestTypes.get(0).getDescription())) {//any test in the build
            //add failed tests
            int i = 1;
            int index = 1;
            while (props.getProperty("Test" + index) != null) {
                props.put("FailedTest" + index, props.getProperty("Test" + index));
                index++;
            }

            //add number of reruns
            if (!StringUtils.isEmpty(this.numberOfReruns)) {
                props.put("Reruns" + i, this.numberOfReruns);
            }

            //add cleanup test
            if (!StringUtils.isEmpty(this.cleanupTest)) {
                props.put("CleanupTest" + i, this.cleanupTest);
            }

        } else {//specific tests in the build
            //set number of reruns
            int j = 1;
            for (RerunSettingsModel settings : this.rerunSettingsModels) {
                if (settings.getChecked()) {//test is selected
                    props.put("FailedTest" + j, settings.getTest());
                    props.put("Reruns" + j, String.valueOf(settings.getNumberOfReruns()));
                    if (!StringUtils.isEmpty(settings.getCleanupTest())) {
                        props.put("CleanupTest" + j, settings.getCleanupTest());
                    }
                    j++;
                }
            }
        }
    }

    @Extension
    public static class DescriptorImpl extends Descriptor<UftSettingsModel> {
        @Nonnull
        @Override
        public String getDisplayName() {
            return "UFT Settings Model";
        }

        public FormValidation doCheckNumberOfReruns(@QueryParameter String value) {
            return UftToolUtils.doCheckNumberOfReruns(value);
        }
    }
}
