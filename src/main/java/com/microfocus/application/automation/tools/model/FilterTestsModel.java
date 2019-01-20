package com.microfocus.application.automation.tools.model;

import hudson.EnvVars;
import hudson.Extension;
import hudson.model.AbstractDescribableImpl;
import hudson.model.Descriptor;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;

import javax.annotation.Nonnull;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

public class FilterTestsModel extends AbstractDescribableImpl<FilterTestsModel> {
    private String testName;
    private List<Boolean> statuses;
    //private List<TestStatusModel> statuses;

    public final static EnumDescription passedTests = new EnumDescription(
            "Passed", "Passed");
    public final static EnumDescription failedTests = new EnumDescription(
            "Failed", "Failed");
    public final static EnumDescription noRunTests = new EnumDescription(
            "NoRun", "No Run");
    public final static EnumDescription notCompleteTests = new EnumDescription(
            "NotCompleted", "Not Completed");
    public final static EnumDescription blockedTests = new EnumDescription(
            "Blocked", "Blocked");
    public final static List<EnumDescription> filterTestsBy = Arrays.asList(
            blockedTests, failedTests, noRunTests, notCompleteTests, passedTests);

    @DataBoundConstructor
    public FilterTestsModel(String testName,  List<Boolean> statuses) {
        this.testName = testName;
        this.statuses = statuses;
    }

    public String getTestName() {
        return testName;
    }

    @DataBoundSetter
    public void setTestName(String testName) {
        this.testName = testName;
    }

    /*public List<TestStatusModel> getStatuses() {
        return statuses;
    }

    @DataBoundSetter
    public void setStatuses(List<TestStatusModel> statuses) {
        this.statuses = statuses;
    }*/

    public List<Boolean> getStatuses() {
        return statuses;
    }

    @DataBoundSetter
    public void setStatuses(List<Boolean> statuses) {
        this.statuses = statuses;
    }

    public void addProperties(Properties props, EnvVars envVars) {
        props.put("FilterTests", "true");
        props.put("TestName", this.testName);
        if(this.statuses.size() > 0){
            int index = 0;
            StringBuilder statusList = new StringBuilder();
            for (Boolean status: statuses) {
                if(status) {
                   statusList.append(", ");
                   statusList.append(filterTestsBy.get(index));
                }
                index++;
            }
            props.put("FilterTests", statusList);
        }
    }

    @Extension
    public static class DescriptorImpl extends Descriptor<FilterTestsModel> {
        @Nonnull
        public String getDisplayName() {return "Filter tests model";}

        public List<EnumDescription> getAlmFilters() {
            return filterTestsBy;
        }
    }
}
