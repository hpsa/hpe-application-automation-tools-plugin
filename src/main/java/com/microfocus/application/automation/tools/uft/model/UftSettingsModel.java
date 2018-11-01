package com.microfocus.application.automation.tools.uft.model;

import com.microfocus.application.automation.tools.model.EnumDescription;
import hudson.Extension;
import hudson.model.AbstractDescribableImpl;
import hudson.model.Descriptor;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;

import javax.annotation.Nonnull;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

public class UftSettingsModel extends AbstractDescribableImpl<UftSettingsModel> {
    private String fsTestPath;
    private String numberOfReruns;
    private String cleanupTest;
    private String onCheckFailedTest;
    private String fsTestType;
    private List<RerunSettingsModel> rerunSettingModels;

    public final static EnumDescription ANY_BUILD_TEST = new EnumDescription("Of any of the build's tests", "Of any of the build's tests");
    public final static EnumDescription SPECIFIC_BUILD_TEST = new EnumDescription("Of a specific test in the build", "Of a specific test in the build");

    public final static List<EnumDescription> fsTestTypes = Arrays.asList(ANY_BUILD_TEST, SPECIFIC_BUILD_TEST);

    @DataBoundConstructor
    public UftSettingsModel(String fsTestPath){
        this.fsTestPath = fsTestPath;
        List<String> testPaths = getTests(getBuildTests(), rerunSettingModels);
        for(String testPath : testPaths) {
            if (!listContainsTest(rerunSettingModels, testPath)) {
                rerunSettingModels.add(new RerunSettingsModel(testPath, false, 0, ""));
            }
        }

        this.setRerunSettingModels(rerunSettingModels);
    }


    public UftSettingsModel(String fsTestPath, String numberOfReruns, String cleanupTest, String onCheckFailedTest, String fsTestType,
                            List<RerunSettingsModel> rerunSettingModels) {
        this.fsTestPath = fsTestPath;
        this.numberOfReruns = numberOfReruns;
        this.cleanupTest = cleanupTest;
        this.onCheckFailedTest = onCheckFailedTest;
        this.fsTestType = fsTestType;
        List<String> testPaths = getTests(getBuildTests(), rerunSettingModels);
        for(String testPath : testPaths) {
            if (!listContainsTest(rerunSettingModels, testPath)) {
                rerunSettingModels.add(new RerunSettingsModel(testPath, false, 0, ""));
            }
        }

        this.setRerunSettingModels(rerunSettingModels);
    }

    public String getFsTestPath() {
        return fsTestPath;
    }

    @DataBoundSetter
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

    /*public List<RerunSettingsModel> getRerunSettingModels() {
        return rerunSettingModels;
    }*/

    @DataBoundSetter
    public void setRerunSettingModels(List<RerunSettingsModel> rerunSettingModels) {
        this.rerunSettingModels = rerunSettingModels;
    }

    /**
     * Gets the rerun settings
     *
     * @return the rerun settings
     */
    public List<RerunSettingsModel> getRerunSettingModels(){
        List<String> testPaths = getTests(getBuildTests(), rerunSettingModels);
        for(String testPath : testPaths){
            if(!listContainsTest(rerunSettingModels, testPath)) {
                rerunSettingModels.add(new RerunSettingsModel(testPath, false, 0, ""));
            }
        }

        return rerunSettingModels;
    }

    public List<EnumDescription> getFsTestTypes() { return fsTestTypes; }


    /**
     * Retrieves the build tests
     *
     * @return an mtbx file with tests, a single test or a list of tests from test folder
     */
    public List<String> getBuildTests() {
        //String directory = "C:\\Users\\laakso\\Documents\\UFT_tests";
        String directory = this.fsTestPath;
        String directoryPath = directory.replace("\\", "/").trim();

        final File folder = new File(directoryPath);

        List<String> buildTests = listFilesForFolder(folder);

        return buildTests;
    }

    /**
     * Retrieves the mtbx path, a test path or the list of tests inside a folder
     *
     * @param folder the test path setup in the configuration (can be the an mtbx file, a single test or a folder containing other tests)
     * @return a list of tests
     */
    private List<String> listFilesForFolder(final File folder) {
        List<String> buildTests = new ArrayList<>();
        if(folder.isDirectory()) {
            for (final File fileEntry : folder.listFiles()) {
                if (fileEntry.isDirectory()) {
                    if (!fileEntry.getName().contains("Action")) {
                        buildTests.add(fileEntry.getPath().trim());
                    } else {
                        buildTests.add(folder.getPath().trim()); //single test
                        break;
                    }
                }
            }
        } else {//mtbx file
            if (folder.getName().contains("mtbx")) {//mtbx file
                buildTests.add(folder.getPath().trim());
            }
        }

        return buildTests;
    }

    /**
     * Checks if a list of tests contains another test
     *
     * @param rerunSettingModels the list of tests
     * @param test the verified test
     * @return true if the list already contains the test, false otherwise
     */
    private Boolean listContainsTest(List<RerunSettingsModel> rerunSettingModels, String test){
        for(RerunSettingsModel settings : rerunSettingModels) {
            if(settings.getTest().trim().equals(test.trim())){
                return true;
            }
        }

        return false;
    }

    /**
     * Updates the list of current tests based on the updated list of build tests
     *
     * @param buildTests the list of build tests setup in the configuration
     * @param rerunSettingModels the list of current tests
     * @return the updated list of tests to rerun
     */
    private List<String> getTests(List<String> buildTests, List<RerunSettingsModel> rerunSettingModels){
        List<String> rerunTests = new ArrayList<>();
        for(RerunSettingsModel rerun : rerunSettingModels){
            rerunTests.add(rerun.getTest().trim());
        }

        for(String test :  buildTests){
            if(!rerunTests.contains(test)){
                rerunTests.add(test.trim());
            }
        }

        for (Iterator<RerunSettingsModel> it = rerunSettingModels.iterator(); it.hasNext() ;)
        {
            RerunSettingsModel rerunSettingsModel1 = it.next();
            if(!buildTests.contains(rerunSettingsModel1.getTest().trim())){
                rerunTests.remove(rerunSettingsModel1.getTest());
                it.remove();
            }
        }

        return rerunTests;
    }

    @Extension
    public static class DescriptorImpl extends Descriptor<UftSettingsModel> {
        @Nonnull
        public String getDisplayName() {return "UFT Settings Model";}
    }
}
