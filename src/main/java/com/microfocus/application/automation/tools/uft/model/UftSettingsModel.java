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

import com.microfocus.application.automation.tools.UftToolUtils;
import com.microfocus.application.automation.tools.model.EnumDescription;
import hudson.Extension;
import hudson.model.AbstractDescribableImpl;
import hudson.model.Descriptor;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import org.apache.commons.lang.StringUtils;

public class UftSettingsModel extends AbstractDescribableImpl<UftSettingsModel> {
    private String fsTestPath;
    private String numberOfReruns;
    private String cleanupTest;
    private String onCheckFailedTest;
    private String fsTestType;
    private List<RerunSettingsModel> rerunSettingsModels;

    public final static EnumDescription ANY_BUILD_TEST = new EnumDescription("Of any of the build's tests", "Of any of the build's tests");
    public final static EnumDescription SPECIFIC_BUILD_TEST = new EnumDescription("Of a specific test in the build", "Of a specific test in the build");

    public final static List<EnumDescription> fsTestTypes = Arrays.asList(ANY_BUILD_TEST, SPECIFIC_BUILD_TEST);

    @DataBoundConstructor
    public UftSettingsModel(String numberOfReruns, String cleanupTest, String onCheckFailedTest, String fsTestType,
                            List<RerunSettingsModel> rerunSettingsModels) {
        this.numberOfReruns = numberOfReruns;
        this.cleanupTest = cleanupTest;
        this.onCheckFailedTest = onCheckFailedTest;
        this.fsTestType = fsTestType;
        this.setRerunSettingsModels(UftToolUtils.getSettings(getFsTestPath(), rerunSettingsModels));
    }

    public String getFsTestPath() {
        return fsTestPath;
    }

    public void setFsTestPath(String fsTestPath) {
        this.fsTestPath = fsTestPath;
        UftToolUtils.getSettings(this.fsTestPath, rerunSettingsModels);
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

    @DataBoundSetter
    public void setRerunSettingsModels(List<RerunSettingsModel> rerunSettingsModels) {
        this.rerunSettingsModels = rerunSettingsModels;
    }

    /**
     * Gets the rerun settings
     *
     * @return the rerun settings
     */
    public List<RerunSettingsModel> getRerunSettingsModels(){
        return UftToolUtils.getSettings(fsTestPath, rerunSettingsModels);
    }

    public List<EnumDescription> getFsTestTypes() { return fsTestTypes; }

    /**
     * Add properties (failed tests, cleanup tests, number of reruns) to properties file
     *
     * @param props
     */
    public void addToProperties(Properties props){
        List<String> buildTestsList = new ArrayList<>();
        int index = 1;
        while(props.getProperty("Test" +  index) != null){
            buildTestsList.add(props.getProperty("Test" +  index));
            index++;
        }

        if(!StringUtils.isEmpty(this.onCheckFailedTest)){
            props.put("onCheckFailedTest", this.onCheckFailedTest);
        } else {
            props.put("onCheckFailedTest", "");
        }

        props.put("testType", this.fsTestType);

        if(this.fsTestType.equals(fsTestTypes.get(0).getDescription())){//any test in the build
            //add failed tests
            int i = 1;
            index = 1;
            for(String failedTest : buildTestsList){
                props.put("FailedTest" + i, failedTest);
                i++;
            }

            //add number of reruns
            if(!StringUtils.isEmpty(this.numberOfReruns)){
                props.put("Reruns" + index, this.numberOfReruns);
            }

            //add cleanup test
            if(!StringUtils.isEmpty(this.cleanupTest)){
                props.put("CleanupTest" + index, this.cleanupTest);
            }

        } else {//specific tests in the build
            //set number of reruns
            List<String> selectedTests = new ArrayList<>();
            List<String> cleanupTests = new ArrayList<>();
            List<Integer> reruns = new ArrayList<>();
            for(RerunSettingsModel settings : this.rerunSettingsModels){
                if(settings.getChecked()){//test is selected
                    selectedTests.add(settings.getTest());
                    reruns.add(settings.getNumberOfReruns());
                    cleanupTests.add(settings.getCleanupTest());
                }
            }

            if(!selectedTests.isEmpty()){//there are tests selected for rerun
                int j;
                for(int i = 0; i < selectedTests.size(); i++){
                    j = i + 1;
                    props.put("FailedTest" + j, selectedTests.get(i));
                    props.put("Reruns" + j, String.valueOf(reruns.get(i)));
                    props.put("CleanupTest" + j, cleanupTests.get(i));
                }
            }
        }
    }

    @Extension
    public static class DescriptorImpl extends Descriptor<UftSettingsModel> {
        @Nonnull
        public String getDisplayName() {return "UFT Settings Model";}
    }
}
