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

package com.microfocus.application.automation.tools.uft.utils;

import com.microfocus.application.automation.tools.uft.model.RerunSettingsModel;
import hudson.EnvVars;
import hudson.FilePath;
import hudson.model.Node;
import hudson.util.FormValidation;
import jenkins.model.Jenkins;
import org.apache.commons.lang.StringUtils;
import org.kohsuke.stapler.QueryParameter;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Logger;

import static groovy.util.ObjectGraphBuilder.NODE_NAME;

public class UftToolUtils {

    private static final Logger logger = Logger.getLogger(UftToolUtils.class.getName());

    private UftToolUtils(){}

    /**
     * Update rerun settings list
     *
     * @param fsTestPath the build tests path
     * @param rerunSettingsModels the rerun settings models to update
     * @return
     */
    public static List<RerunSettingsModel> updateRerunSettings(String nodeName, String fsTestPath, List<RerunSettingsModel> rerunSettingsModels){
        List<String> testPaths = UftToolUtils.getTests(UftToolUtils.getBuildTests(nodeName, fsTestPath), rerunSettingsModels);
        for(String testPath : testPaths){
            if(!UftToolUtils.listContainsTest(rerunSettingsModels, testPath)) {
                rerunSettingsModels.add(new RerunSettingsModel(testPath, false, 0, ""));
            }
        }

        return rerunSettingsModels;
    }

    /**
     * Retrieves the build tests
     *
     * @return an mtbx file with tests, a single test or a list of tests from test folder
     */
    public static List<String> getBuildTests(String nodeName, String fsTestPath) {
        if(fsTestPath != null) {
            List<String> buildTests;
            Node node = Jenkins.getInstance().getNode(nodeName);
            String directoryPath = fsTestPath.replace("\\", "/").trim();

            if(Jenkins.getInstance().getNodes().isEmpty() || (node == null)){//run tests on master
                buildTests = listFilesForFolder(new File(directoryPath));
            } else {//run tests on selected node
                buildTests = getTestsFromNode(nodeName, directoryPath);
            }
            return buildTests;
        }

        return null;
    }

    public static List<String> getTestsFromNode(String nodeName, String path){
        Node node = Jenkins.getInstance().getNode(nodeName);
        FilePath filePath = new FilePath(node.getChannel(), path);
        UftMasterToSlave uftMasterToSlave = new UftMasterToSlave();
        List<String> tests = new ArrayList<>();
        try {
            tests = filePath.act(uftMasterToSlave);//invoke listFilesForFolder
        } catch (IOException e) {
            logger.info(String.format("File path not found %s", e.getMessage()));
        } catch (InterruptedException e) {
            logger.info(String.format("Remote operation failed %s", e.getMessage()));
        }

        return tests;
    }

    /**
     * Retrieves the mtbx path, a test path or the list of tests inside a folder
     *
     * @param folder the test path setup in the configuration (can be the an mtbx file, a single test or a folder containing other tests)
     * @return a list of tests
     */
    public static List<String> listFilesForFolder(final File folder) {
        List<String> buildTests = new ArrayList<>();
        if(!folder.isDirectory() && folder.getName().contains("mtbx")) {
            buildTests.add(folder.getPath().trim());
            return buildTests;
        }
        if(folder.isDirectory()) {
            for (final File fileEntry : folder.listFiles()) {
                if (fileEntry.isDirectory()) {
                    if(fileEntry.getName().contains("Action")){
                        buildTests.add(folder.getPath().trim());//single test
                        break;
                    } else {
                        buildTests.add(fileEntry.getPath().trim());
                    }
                }
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
    public static Boolean listContainsTest(List<RerunSettingsModel> rerunSettingModels, String test){
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
    public static List<String> getTests(List<String> buildTests, List<RerunSettingsModel> rerunSettingModels){
        List<String> rerunTests = new ArrayList<>();
        if(buildTests == null || rerunSettingModels == null){
            return rerunTests;
        }

        for(RerunSettingsModel rerun : rerunSettingModels){
            rerunTests.add(rerun.getTest().trim());
        }

        for(String test :  buildTests){
            if(!rerunTests.contains(test)){
                rerunTests.add(test.trim());
            }
        }

        for (Iterator<RerunSettingsModel> it = rerunSettingModels.iterator(); it.hasNext();)
        {
            RerunSettingsModel rerunSettingsModel1 = it.next();
            if(!buildTests.contains(rerunSettingsModel1.getTest().trim())){
                rerunTests.remove(rerunSettingsModel1.getTest());
                it.remove();
            }
        }

        return rerunTests;
    }

    public static FormValidation doCheckNumberOfReruns(final String value) {

        String errorMessage = "You must enter a positive integer number.";

        try {
            int number = Integer.parseInt(value);

            if (StringUtils.isBlank(value.trim()) || number < 0) {
                return FormValidation.error(errorMessage);
            }
        } catch (NumberFormatException e) {
            return FormValidation.error(errorMessage);
        }

        return FormValidation.ok();
    }
}
