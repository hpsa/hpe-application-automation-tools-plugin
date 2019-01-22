package com.microfocus.application.automation.tools.model;

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
    private Boolean blockedCheckbox;
    private Boolean failedCheckbox;
    private Boolean notCompletedCheckbox;
    private Boolean noRunCheckbox;
    private Boolean passedCheckbox;


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
    public FilterTestsModel(String testName, Boolean blockedCheckbox, Boolean failedCheckbox,
                            Boolean notCompletedCheckbox, Boolean noRunCheckbox, Boolean passedCheckbox) {
        this.testName = testName;
        this.blockedCheckbox = blockedCheckbox;
        this.failedCheckbox = failedCheckbox;
        this.notCompletedCheckbox = notCompletedCheckbox;
        this.noRunCheckbox = noRunCheckbox;
        this.passedCheckbox = passedCheckbox;
    }

    public String getTestName() {
        return testName;
    }

    @DataBoundSetter
    public void setTestName(String testName) {
        this.testName = testName;
    }

    public Boolean getBlockedCheckbox() {
        return blockedCheckbox;
    }

    public void setBlockedCheckbox(Boolean blockedCheckbox) {
        this.blockedCheckbox = blockedCheckbox;
    }

    public Boolean getFailedCheckbox() {
        return failedCheckbox;
    }

    public void setFailedCheckbox(Boolean failedCheckbox) {
        this.failedCheckbox = failedCheckbox;
    }

    public Boolean getNotCompletedCheckbox() {
        return notCompletedCheckbox;
    }

    public void setNotCompletedCheckbox(Boolean notCompletedCheckbox) {
        this.notCompletedCheckbox = notCompletedCheckbox;
    }

    public Boolean getNoRunCheckbox() {
        return noRunCheckbox;
    }

    public void setNoRunCheckbox(Boolean noRunCheckbox) {
        this.noRunCheckbox = noRunCheckbox;
    }

    public Boolean getPassedCheckbox() {
        return passedCheckbox;
    }

    public void setPassedCheckbox(Boolean passedCheckbox) {
        this.passedCheckbox = passedCheckbox;
    }

    public void addProperties(Properties props) {
        props.put("FilterTests", "true");
        props.put("FilterByName", this.testName);

        StringBuilder statusList = new StringBuilder();
        if(blockedCheckbox){
           statusList.append("Blocked");
           statusList.append(", ");
        }
        if(failedCheckbox){
            statusList.append("Failed");
            statusList.append(", ");
        }
        if(notCompletedCheckbox){
            statusList.append("Not Completed");
            statusList.append(", ");
        }
        if(noRunCheckbox){
            statusList.append("No Run");
            statusList.append(", ");
        }
        if(passedCheckbox){
            statusList.append("Passed");
            statusList.append(", ");
        }

        if(statusList.length() > 0){
            statusList.replace(statusList.lastIndexOf(","), statusList.lastIndexOf(" "),"");
        }
        props.put("FilterByStatus", statusList.toString());
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
