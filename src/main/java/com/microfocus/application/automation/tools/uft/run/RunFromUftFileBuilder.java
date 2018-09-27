/*
 * Certain versions of software and/or documents ("Material") accessible here may contain branding from
 * Hewlett-Packard Company (now HP Inc.) and Hewlett Packard Enterprise Company.  As of September 1, 2017,
 * the Material is now offered by Micro Focus, a separately owned and operated company.  Any reference to the HP
 * and Hewlett Packard Enterprise/HPE marks is historical in nature, and the HP and Hewlett Packard Enterprise/HPE
 * marks are the property of their respective owners.
 * __________________________________________________________________
 * MIT License
 *
 * (c) Copyright 2012-2018 Micro Focus or one of its affiliates.
 *
 * The only warranties for products and services of Micro Focus and its affiliates
 * and licensors ("Micro Focus") are set forth in the express warranty statements
 * accompanying such products and services. Nothing herein should be construed as
 * constituting an additional warranty. Micro Focus shall not be liable for technical
 * or editorial errors or omissions contained herein.
 * The information contained herein is subject to change without notice.
 * ___________________________________________________________________
 */

package com.microfocus.application.automation.tools.uft.run;

import com.microfocus.application.automation.tools.model.FileSystemTestSetModel;
import com.microfocus.application.automation.tools.model.RunFromFileSystemModel;
import com.microfocus.application.automation.tools.run.RunFromFileBuilder;
import hudson.EnvVars;
import hudson.FilePath;
import hudson.model.Result;
import hudson.model.Run;
import hudson.model.TaskListener;
import org.apache.commons.io.IOUtils;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Describs a regular jenkins build step from UFT or LR
 */
public class RunFromUftFileBuilder {
    private FileSystemTestSetModel fileSystemTestSetModel;

    public RunFromUftFileBuilder(FileSystemTestSetModel fileSystemTestSetModel) {
        this.fileSystemTestSetModel = fileSystemTestSetModel;
    }

    /**
     * Replace the fsTests given as mtbx with the actual mtbx file.
     *
     * @param workspace the current workspace
     * @param props     the properties
     * @param content   the mtbx content
     * @param key       the test key
     * @param time      current time string
     * @param index     the index for the prefix
     * @throws Exception
     */
    public static void replaceTestWithMtbxFile(FilePath workspace, Properties props, String content, String key, String time, int index) throws Exception {
        if (RunFromFileSystemModel.isMtbxContent(content)) {
            try {
                String prefix = index > 0 ? index + "_" : "";
                String mtbxFilePath = prefix + createMtbxFileInWs(workspace, content, time);
                props.setProperty(key, mtbxFilePath);
            } catch (IOException | InterruptedException e) {
                throw new Exception(e);
            }
        }
    }

    /**
     * Replace the fsTests given as mtbx with the actual mtbx file.
     *
     * @param workspace the current workspace
     * @param props     the properties
     * @param content   the mtbx content
     * @param key       the test key
     * @param time      current time string
     * @throws Exception
     */
    public static void replaceTestWithMtbxFile(FilePath workspace, Properties props, String content, String key, String time) throws Exception {
        replaceTestWithMtbxFile(workspace, props, content, key, time, 0);
    }

    /**
     * Creates an .mtbx file with the provided mtbx content.
     *
     * @param workspace   jenkins workspace
     * @param mtbxContent the motbx content
     * @param timeString  current time represented as a String
     * @return the remote file path
     * @throws IOException
     * @throws InterruptedException
     */
    private static String createMtbxFileInWs(FilePath workspace, String mtbxContent, String timeString) throws IOException, InterruptedException {
        String fileName = "test_suite_" + timeString + ".mtbx";

        FilePath remoteFile = workspace.child(fileName);

        String mtbxContentUpdated = mtbxContent.replace("${WORKSPACE}", workspace.getRemote());
        InputStream in = IOUtils.toInputStream(mtbxContentUpdated, "UTF-8");
        remoteFile.copyFrom(in);

        return remoteFile.getRemote();
    }

    public void setFileSystemTestSetModel(FileSystemTestSetModel fileSystemTestSetModel) {
        this.fileSystemTestSetModel = fileSystemTestSetModel;
    }

    public void replaceTestWithMtbxForNonParallelRunner(@Nonnull Run<?, ?> build, @Nonnull FilePath workspace, @Nonnull TaskListener listener, Properties mergedProperties, String time, String firstTestKey, String firstTestContent) {
        try {
            replaceTestWithMtbxFile(workspace, mergedProperties, firstTestContent, firstTestKey, time);
        } catch (Exception e) {
            build.setResult(Result.FAILURE);
            listener.error("Failed to save MTBX file : " + e.getMessage());
        }
    }

    public void replaceTestWithMtbxForParallelRunner(@Nonnull Run<?, ?> build, @Nonnull FilePath workspace, @Nonnull TaskListener listener, EnvVars env, Properties mergedProperties, String time, RunFromFileBuilder runFromFileBuilder) {
        // add the parallel runner properties
        fileSystemTestSetModel.addTestSetProperties(mergedProperties, env);

        // we need to replace each mtbx test with mtbx file path
        for (int index = 1; index < fileSystemTestSetModel.getFileSystemTestSet().size(); index++) {
            String key = "Test" + index;
            String content = mergedProperties.getProperty(key + index, "");
            try {
                replaceTestWithMtbxFile(workspace, mergedProperties, content, key, time, index);
            } catch (Exception e) {
                build.setResult(Result.FAILURE);
                listener.error("Failed to save MTBX file : " + e.getMessage());
            }
        }
    }
}
