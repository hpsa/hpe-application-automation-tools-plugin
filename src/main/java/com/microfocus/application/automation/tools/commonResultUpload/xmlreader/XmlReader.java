/*
 * Copyright (c) 2012 Hewlett-Packard Development Company, L.P.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package com.microfocus.application.automation.tools.commonResultUpload.xmlreader;

import com.microfocus.application.automation.tools.commonResultUpload.CommonUploadLogger;
import com.microfocus.application.automation.tools.commonResultUpload.xmlreader.model.EntitiesFieldMap;
import com.microfocus.application.automation.tools.commonResultUpload.xmlreader.model.XmlResultEntity;
import hudson.FilePath;
import hudson.model.Result;
import hudson.model.Run;
import org.apache.tools.ant.DirectoryScanner;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class XmlReader {

    private CommonUploadLogger logger;
    private Run<?, ?> run;
    private FilePath workspace;

    public XmlReader(Run<?, ?> run, FilePath workspace, CommonUploadLogger logger) {
        this.run = run;
        this.logger = logger;
        this.workspace = workspace;
    }

    public List<XmlResultEntity> scan(String filePath, EntitiesFieldMap entitiesFieldMap) {
        DirectoryScanner ds = new DirectoryScanner();
        ds.setBasedir(run.getRootDir());
        ds.setIncludes(new String[] {filePath});
        ds.scan();

        if (ds.getIncludedFilesCount() == 0) {
            logger.info("No Test Report found in job folder: " + run.getRootDir().getAbsolutePath());
            return scanInWorkspace(filePath, entitiesFieldMap);
        } else {
            logger.info(ds.getIncludedFilesCount() + " test result file found in job folder: " + run.getRootDir().getAbsolutePath());
            return read(ds.getIncludedFiles(), entitiesFieldMap);
        }
    }

    private List<XmlResultEntity> scanInWorkspace(String filePath, EntitiesFieldMap entitiesFieldMap) {
        List<FilePath> files = new ArrayList<>();
        for (String include : new String[] {filePath}) {
            try {
                files.addAll(Arrays.asList(workspace.list(include)));
            } catch (Exception e) {
                logger.error(e.getMessage());
                run.setResult(Result.FAILURE);
            }
        }
        if (files.size() < 1) {
            logger.info("No Test Report found in workspace: " + workspace);
        }
        logger.info(files.size() + " test result file found in workspace: " + workspace);
        return readWorkspace(files, entitiesFieldMap);
    }

    private List<XmlResultEntity> readWorkspace(List<FilePath> files, EntitiesFieldMap entitiesFieldMap) {
        List<XmlResultEntity> xmlResultEntities = new ArrayList<>();
        for (FilePath file : files) {
            try {
                TestSetReader tr = new TestSetReader(file, entitiesFieldMap);
                xmlResultEntities.addAll(tr.readTestsets());
            } catch (Exception e) {
                logger.error(e.getMessage());
                e.printStackTrace();
                run.setResult(Result.FAILURE);
            }
        }
        return xmlResultEntities;
    }

    private List<XmlResultEntity> read(String[] files, EntitiesFieldMap entitiesFieldMap) {
        List<XmlResultEntity> xmlResultEntities = new ArrayList<>();
        for (String fileName : files) {
            String fullpath = run.getRootDir().getAbsolutePath() + File.separator + fileName;
            try {
                TestSetReader tr = new TestSetReader(fullpath, entitiesFieldMap);
                xmlResultEntities.addAll(tr.readTestsets());
            } catch (Exception e) {
                logger.error(e.getMessage());
                e.printStackTrace();
                run.setResult(Result.FAILURE);
            }
        }
        return xmlResultEntities;
    }
}
