/*
 *
 *  *
 *  *  Certain versions of software and/or documents (“Material”) accessible here may contain branding from
 *  *  Hewlett-Packard Company (now HP Inc.) and Hewlett Packard Enterprise Company.  As of September 1, 2017,
 *  *  the Material is now offered by Micro Focus, a separately owned and operated company.  Any reference to the HP
 *  *  and Hewlett Packard Enterprise/HPE marks is historical in nature, and the HP and Hewlett Packard Enterprise/HPE
 *  *  marks are the property of their respective owners.
 *  * __________________________________________________________________
 *  * MIT License
 *  *
 *  * © Copyright 2012-2019 Micro Focus or one of its affiliates..
 *  *
 *  * The only warranties for products and services of Micro Focus and its affiliates
 *  * and licensors (“Micro Focus”) are set forth in the express warranty statements
 *  * accompanying such products and services. Nothing herein should be construed as
 *  * constituting an additional warranty. Micro Focus shall not be liable for technical
 *  * or editorial errors or omissions contained herein.
 *  * The information contained herein is subject to change without notice.
 *  * ___________________________________________________________________
 *  *
 *
 */

package com.microfocus.application.automation.tools.results.service;

import com.microfocus.application.automation.tools.rest.RestClient;
import com.microfocus.application.automation.tools.results.service.rest.CreateAttachment;
import com.microfocus.application.automation.tools.sse.sdk.Logger;
import com.microfocus.application.automation.tools.sse.sdk.Response;
import hudson.FilePath;
import hudson.model.Run;
import org.apache.commons.io.IOUtils;
import org.apache.tools.ant.DirectoryScanner;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;

public class AttachmentUploadService {

    private Run<?, ?> run;
    private FilePath workspace;
    private RestClient restClient;
    private Logger logger;

    private static AttachmentUploadService aus;

    public static void init(Run<?, ?> run, FilePath workspace, RestClient restClient, Logger logger) {
        aus = new AttachmentUploadService(run, workspace, restClient, logger);
    }

    public static AttachmentUploadService getInstance() {
        return aus;
    }

    private AttachmentUploadService(Run<?, ?> run, FilePath workspace, RestClient restClient, Logger logger) {
        this.run = run;
        this.workspace = workspace;
        this.restClient = restClient;
        this.logger = logger;
    }

    public boolean upload(String fileName, String entityCollectionName, String entityId) {
        DirectoryScanner ds = new DirectoryScanner();
        ds.setBasedir(run.getRootDir());
        ds.setIncludes(new String[] {fileName});
        ds.scan();

        boolean result = true;

        if (ds.getIncludedFilesCount() > 0) {
            // Find in build folder.
            for (int i = 0; i < ds.getIncludedFilesCount(); i++) {
                String filePath = run.getRootDir().getAbsolutePath() + File.separator + ds.getIncludedFiles()[i];
                logger.log("INFO: Fould file: " + filePath);

                try (FileInputStream in = new FileInputStream(new File(filePath))) {
                    result = upload(IOUtils.toByteArray(in), ds.getIncludedFiles()[i], entityCollectionName, entityId);
                } catch (IOException e) {
                    logger.log("ERR: Read file failed. " + e.getMessage());
                    result = false;
                }
            }
        } else {
            // Find in workspace
            FilePath[] fileList = new FilePath[0];
            try {
                fileList = workspace.list(fileName);
            } catch (IOException | InterruptedException e) {
                logger.log("ERR: List  " + fileName + " in workspace failed. " + e.getMessage());
                result = false;
            }

            for (FilePath f : fileList) {
                logger.log("INFO: Fould file: " + f.getRemote() + "| name: " + f.getName());

                try (InputStream in  = f.read()) {
                    logger.log("INFO: InputSteam read get: " + in.toString());
                    result = upload(IOUtils.toByteArray(in), f.getName(), entityCollectionName, entityId);
                } catch (IOException | InterruptedException e) {
                    logger.log("ERR: Read file failed. " + e.getMessage());
                    result = false;
                }
            }
        }

        return result;
    }

    private boolean upload(byte[] fileContent, String filename, String entityCollectionName, String entityId) {
        logger.log("INFO: Uploading file: " + filename);
        CreateAttachment ca = new CreateAttachment(entityCollectionName,
                restClient,
                entityId,
                filename,
                fileContent);
        Response re = ca.perform();

        if (re.getStatusCode() != HttpURLConnection.HTTP_CREATED) {
            logger.log("INFO: Stauts " + re.getStatusCode());
            logger.log("ERR: Attachment upload failed. " + re.getFailure());
            logger.log("ERR: " + new String(re.getData()));
            return false;
        } else {
            logger.log("INFO: Attachment upload success.");
            return true;
        }
    }
}
