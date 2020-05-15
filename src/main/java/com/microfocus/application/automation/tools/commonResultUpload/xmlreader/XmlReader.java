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
import hudson.model.Result;
import hudson.model.Run;
import org.apache.tools.ant.DirectoryScanner;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class XmlReader {

    private CommonUploadLogger logger;
    private Run<?, ?> run;

    public XmlReader(Run<?, ?> run, CommonUploadLogger logger) {
        this.run = run;
        this.logger = logger;
    }

    public List<XmlResultEntity> read(String filePath, EntitiesFieldMap entitiesFieldMap) {
        DirectoryScanner ds = new DirectoryScanner();
        ds.setBasedir(run.getRootDir());
        ds.setIncludes(new String[] {filePath});
        ds.scan();

        List<XmlResultEntity> xmlResultEntities = new ArrayList<>();
        if (ds.getIncludedFilesCount() == 0) {
            logger.info("No Test Report found.");
        } else {
            logger.info(ds.getIncludedFilesCount() + " test result file found.");
            String[] files = ds.getIncludedFiles();
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
        }
        return xmlResultEntities;
    }
}
