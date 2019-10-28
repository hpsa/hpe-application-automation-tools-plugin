/*
 * Certain versions of software and/or documents ("Material") accessible here may contain branding from
 * Hewlett-Packard Company (now HP Inc.) and Hewlett Packard Enterprise Company.  As of September 1, 2017,
 * the Material is now offered by Micro Focus, a separately owned and operated company.  Any reference to the HP
 * and Hewlett Packard Enterprise/HPE marks is historical in nature, and the HP and Hewlett Packard Enterprise/HPE
 * marks are the property of their respective owners.
 * __________________________________________________________________
 * MIT License
 *
 * (c) Copyright 2012-2019 Micro Focus or one of its affiliates.
 *
 * The only warranties for products and services of Micro Focus and its affiliates
 * and licensors ("Micro Focus") are set forth in the express warranty statements
 * accompanying such products and services. Nothing herein should be construed as
 * constituting an additional warranty. Micro Focus shall not be liable for technical
 * or editorial errors or omissions contained herein.
 * The information contained herein is subject to change without notice.
 * ___________________________________________________________________
 */

package com.microfocus.application.automation.tools.octane.executor;

import com.hp.octane.integrations.uft.UftTestDiscoveryUtils;
import com.hp.octane.integrations.uft.items.*;
import com.hp.octane.integrations.utils.SdkConstants;
import com.hp.octane.integrations.utils.SdkStringUtils;
import com.microfocus.application.automation.tools.octane.configuration.SDKBasedLoggerProvider;
import hudson.FilePath;
import hudson.model.BuildListener;
import hudson.model.Run;
import hudson.model.TaskListener;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * Service is responsible to detect changes according to SCM change and to put it to queue of UftTestDiscoveryDispatcher
 */
public class UFTTestDetectionService {
    private static final Logger logger = SDKBasedLoggerProvider.getLogger(UFTTestDetectionService.class);
    private static final String INITIAL_DETECTION_FILE = "INITIAL_DETECTION_FILE.txt";
    private static final String DETECTION_RESULT_FILE = "detection_result.json";

    public static UftTestDiscoveryResult startScanning(File rootDir, BuildListener buildListener, String configurationId, String workspaceId, String scmRepositoryId,
                                                       String testRunnerId, UFTTestDetectionCallable.ScmChangesWrapper scmChangesWrapper, boolean fullScan) {
        UftTestDiscoveryResult result = null;
        try {

            boolean myFullScan = fullScan || !initialDetectionFileExist(rootDir);
            if (myFullScan) {
                printToConsole(buildListener, "Executing full sync");
                result = UftTestDiscoveryUtils.doFullDiscovery(rootDir);
            } else {
                printToConsole(buildListener, "Executing changeSet sync. For full sync - define in job boolean parameter 'Full sync' with value 'true'.");
                result = doChangeSetDetection(scmChangesWrapper, rootDir);
                removeTestDuplicatedForUpdateTests(result);
                removeFalsePositiveDataTables(result, result.getDeletedTests(), result.getDeletedScmResourceFiles());
                removeFalsePositiveDataTables(result, result.getNewTests(), result.getNewScmResourceFiles());
            }

            Map<OctaneStatus, Integer> testStatusMap = computeStatusMap(result.getAllTests());
            for (Map.Entry<OctaneStatus, Integer> entry : testStatusMap.entrySet()) {
                printToConsole(buildListener, String.format("Found %s tests with status %s", entry.getValue(), entry.getKey()));
            }

            Map<OctaneStatus, Integer> resourceFilesStatusMap = computeStatusMap(result.getAllScmResourceFiles());
            for (Map.Entry<OctaneStatus, Integer> entry : resourceFilesStatusMap.entrySet()) {
                printToConsole(buildListener, String.format("Found %s data tables with status %s", entry.getValue(), entry.getKey()));
            }

            if (result.isHasQuotedPaths()) {
                printToConsole(buildListener, "This run may not have discovered all updated tests. \n" +
                        "It seems that the changes in this build included filenames with Unicode characters, which Git did not list correctly.\n" +
                        "To make sure Git can properly list such file names, configure Git as follows : git config --global core.quotepath false\n" +
                        "To discover the updated tests that were missed in this run and send them to ALM Octane, run this job manually with the \"Full sync\" parameter selected.\n");
            }

            result.setScmRepositoryId(scmRepositoryId);
            result.setConfigurationId(configurationId);
            result.setWorkspaceId(workspaceId);
            result.setFullScan(fullScan);

            //we add test runner only for discovery jobs that were created for test runners
            if (testRunnerId != null) {
                result.setTestRunnerId(testRunnerId);
            }

            result.sortItems();
            createInitialDetectionFile(rootDir);

        } catch (Exception e) {
            logger.error("Fail in startScanning : " + e.getMessage());
        }

        return result;
    }

    private static Map<OctaneStatus, Integer> computeStatusMap(List<? extends SupportsOctaneStatus> entities) {
        Map<OctaneStatus, Integer> statusMap = new HashMap<>();
        for (SupportsOctaneStatus item : entities) {
            if (!statusMap.containsKey(item.getOctaneStatus())) {
                statusMap.put(item.getOctaneStatus(), 0);
            }
            statusMap.put(item.getOctaneStatus(), statusMap.get(item.getOctaneStatus()) + 1);
        }
        return statusMap;
    }

    /**
     * Deleted data table might be part of deleted test. During discovery its very hard to know.
     * Here we pass through all deleted data tables, if we found data table parent is test folder - we know that the delete was part of test delete
     *
     * @param tests
     * @param scmResourceFiles
     */
    private static void removeFalsePositiveDataTables(UftTestDiscoveryResult result, List<AutomatedTest> tests, List<ScmResourceFile> scmResourceFiles) {
        if (!scmResourceFiles.isEmpty() && !tests.isEmpty()) {

            List<ScmResourceFile> falsePositive = new ArrayList<>();
            for (ScmResourceFile item : scmResourceFiles) {
                int parentSplitterIndex = item.getRelativePath().lastIndexOf(SdkConstants.FileSystem.WINDOWS_PATH_SPLITTER);
                if (parentSplitterIndex != -1) {
                    String parentName = item.getRelativePath().substring(0, parentSplitterIndex);
                    for (AutomatedTest test : tests) {
                        String testPath = SdkStringUtils.isEmpty(test.getPackage()) ? test.getName() : test.getPackage() + SdkConstants.FileSystem.WINDOWS_PATH_SPLITTER + test.getName();
                        if (parentName.contains(testPath)) {
                            falsePositive.add(item);
                            break;
                        }
                    }
                }
            }

            result.getAllScmResourceFiles().removeAll(falsePositive);
        }
    }

    private static void removeTestDuplicatedForUpdateTests(UftTestDiscoveryResult result) {
        Set<String> keys = new HashSet<>();
        List<AutomatedTest> testsToRemove = new ArrayList<>();
        for (AutomatedTest test : result.getUpdatedTests()) {
            String key = test.getPackage() + "_" + test.getName();
            if (keys.contains(key)) {
                testsToRemove.add(test);
            }
            keys.add(key);

        }
        result.getAllTests().removeAll(testsToRemove);
    }

    public static void printToConsole(BuildListener buildListener, String msg) {
        if (buildListener != null) {
            buildListener.getLogger().println("UFTTestDetectionService : " + msg);
        }
    }

    private static UftTestDiscoveryResult doChangeSetDetection(UFTTestDetectionCallable.ScmChangesWrapper scmChangesWrapper, File workspace) {
        UftTestDiscoveryResult result = new UftTestDiscoveryResult();


        for (UFTTestDetectionCallable.ScmChangeAffectedFileWrapper affectedFileWrapper : scmChangesWrapper.getAffectedFiles()) {

            if (affectedFileWrapper.getPath().startsWith("\"")) {
                result.setHasQuotedPaths(true);
            }
            String affectedFileFullPath = workspace + File.separator + affectedFileWrapper.getPath();
            if (!affectedFileWrapper.isSvnDirType()) {
                if (UftTestDiscoveryUtils.isTestMainFilePath(affectedFileWrapper.getPath())) {
                    File testFolder = UftTestDiscoveryUtils.getTestFolderForTestMainFile(affectedFileFullPath);
                    File affectedFile = new File(affectedFileFullPath);
                    boolean fileExist = affectedFile.exists();
                    UftTestType uftTestType = UftTestDiscoveryUtils.getUftTestType(affectedFileWrapper.getPath());

                    AutomatedTest test = UftTestDiscoveryUtils.createAutomatedTest(workspace, testFolder, uftTestType);
                    test.setChangeSetSrc(affectedFileWrapper.getGitSrc());
                    test.setChangeSetDst(affectedFileWrapper.getGitDst());


                    if (UFTTestDetectionCallable.ScmChangeEditTypeWrapper.ADD.equals(affectedFileWrapper.getEditType())) {
                        if (fileExist) {
                            result.getAllTests().add(test);
                        }
                    } else if (UFTTestDetectionCallable.ScmChangeEditTypeWrapper.DELETE.equals(affectedFileWrapper.getEditType())) {
                        if (!fileExist) {
                            test.setOctaneStatus(OctaneStatus.DELETED);
                            test.setExecutable(false);
                            result.getAllTests().add(test);
                        }
                    } else if (UFTTestDetectionCallable.ScmChangeEditTypeWrapper.EDIT.equals(affectedFileWrapper.getEditType())) {
                        if (fileExist) {
                            test.setOctaneStatus(OctaneStatus.MODIFIED);
                            result.getAllTests().add(test);
                        }
                    }
                } else if (UftTestDiscoveryUtils.isUftDataTableFile(affectedFileWrapper.getPath())) {
                    File affectedFile = new File(affectedFileFullPath);
                    ScmResourceFile resourceFile = UftTestDiscoveryUtils.createDataTable(workspace, affectedFile);
                    resourceFile.setChangeSetSrc(affectedFileWrapper.getGitSrc());
                    resourceFile.setChangeSetDst(affectedFileWrapper.getGitDst());

                    if (UFTTestDetectionCallable.ScmChangeEditTypeWrapper.ADD.equals(affectedFileWrapper.getEditType())) {
                        UftTestType testType = UftTestDiscoveryUtils.isUftTestFolder(affectedFile.getParentFile().listFiles());
                        if (testType.isNone()) {
                            if (affectedFile.exists()) {
                                result.getAllScmResourceFiles().add(resourceFile);
                            }
                        }
                    } else if (UFTTestDetectionCallable.ScmChangeEditTypeWrapper.DELETE.equals(affectedFileWrapper.getEditType())) {
                        if (!affectedFile.exists()) {
                            resourceFile.setOctaneStatus(OctaneStatus.DELETED);
                            result.getAllScmResourceFiles().add(resourceFile);
                        }
                    }
                }
            } else { //isDir
                if (UFTTestDetectionCallable.ScmChangeEditTypeWrapper.DELETE.equals(affectedFileWrapper.getEditType())) {

                    FilePath filePath = new FilePath(new File(affectedFileWrapper.getPath()));
                    String deletedFolder = filePath.getRemote().replace(SdkConstants.FileSystem.LINUX_PATH_SPLITTER, SdkConstants.FileSystem.WINDOWS_PATH_SPLITTER);
                    result.getDeletedFolders().add(deletedFolder);
                }
            }
        }

        return result;
    }

    private static boolean initialDetectionFileExist(File rootFile) {
        try {
            File file = new File(rootFile, INITIAL_DETECTION_FILE);
            return file.exists();

        } catch (Exception e) {
            return false;
        }
    }

    private static void createInitialDetectionFile(File rootFile) {
        try {
            File file = new File(rootFile, INITIAL_DETECTION_FILE);
            logger.info("Initial detection file path : " + file.getPath());
            file.createNewFile();
        } catch (IOException e) {
            logger.error("Failed to createInitialDetectionFile : " + e.getMessage());
        }
    }

    /**
     * Serialize detectionResult to file in XML format
     *
     * @param run
     * @param taskListenerLog
     * @param detectionResult
     */
    public static void publishDetectionResults(Run run, TaskListener taskListenerLog, UftTestDiscoveryResult detectionResult) {

        File file = getDetectionResultFile(run);
        try {
            detectionResult.writeToFile(file);
        } catch (Exception e) {
            String msg = "Failed to persist detection results : " + e.getMessage();
            if (taskListenerLog != null) {
                taskListenerLog.error(msg);
            }
            logger.error(msg);
        }
    }

    public static UftTestDiscoveryResult readDetectionResults(Run run) {
        File file = getDetectionResultFile(run);

        try {
            return UftTestDiscoveryResult.readFromFile(file);
        } catch (IOException e) {
            logger.error("Failed to read detection results : " + e.getMessage());
            return null;
        }
    }

    public static File getDetectionResultFile(Run run) {
        return new File(run.getRootDir(), DETECTION_RESULT_FILE);
    }
}