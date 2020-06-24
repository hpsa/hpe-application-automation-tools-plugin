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

package com.microfocus.application.automation.tools.commonResultUpload.uploader;

import com.microfocus.application.automation.tools.commonResultUpload.CommonUploadLogger;
import com.microfocus.application.automation.tools.commonResultUpload.service.CustomizationService;
import com.microfocus.application.automation.tools.commonResultUpload.service.FolderService;
import com.microfocus.application.automation.tools.commonResultUpload.service.RestService;
import com.microfocus.application.automation.tools.commonResultUpload.service.UDFTranslator;
import com.microfocus.application.automation.tools.commonResultUpload.service.VersionControlService;
import com.microfocus.application.automation.tools.commonResultUpload.xmlreader.configloader.RunStatusMapLoader;
import com.microfocus.application.automation.tools.commonResultUpload.xmlreader.model.EntitiesFieldMap;
import com.microfocus.application.automation.tools.commonResultUpload.xmlreader.configloader.EntitiesFieldMapLoader;
import com.microfocus.application.automation.tools.commonResultUpload.xmlreader.model.RunStatusMap;
import com.microfocus.application.automation.tools.commonResultUpload.xmlreader.XmlReader;
import com.microfocus.application.automation.tools.commonResultUpload.xmlreader.model.XmlResultEntity;
import com.microfocus.application.automation.tools.rest.RestClient;
import com.microfocus.application.automation.tools.results.service.AlmRestTool;
import com.microfocus.application.automation.tools.sse.sdk.authenticator.AuthenticationTool;
import hudson.FilePath;
import hudson.model.Run;

import java.util.List;
import java.util.Map;

public class Uploader {

    private RestClient restClient;
    private Map<String, String> params;
    private CommonUploadLogger logger;
    private CustomizationService cs;
    private VersionControlService vs;
    private UDFTranslator udt;
    private RestService rs;
    private FolderService fs;
    private Run<?, ?> run;
    private FilePath workspace;

    public Uploader(Run<?, ?> run, FilePath workspace, CommonUploadLogger logger, Map<String, String> params) {
        this.run = run;
        this.workspace = workspace;
        this.logger = logger;
        this.params = params;
    }

    public void upload() {
        restClient = new RestClient(params.get("almServerUrl"),
                params.get("almDomain"),
                params.get("almProject"),
                params.get("username"));

        boolean login = AuthenticationTool.getInstance().authenticate(restClient,
                params.get("username"), params.get("password"),
                params.get("almServerUrl"), params.get("clientType"), logger);
        if (login) {
            init();
            if (!rs.getDomains().contains(params.get("almDomain"))) {
                logger.error("Invalid domain name:" + params.get("almDomain"));
                return;
            }
            if (!rs.getProjects(params.get("almDomain")).contains(params.get("almProject"))) {
                logger.error("Invalid project name:" + params.get("almProject"));
                return;
            }
            List<XmlResultEntity> xmlResultEntities = getUploadData();
            if (xmlResultEntities == null || xmlResultEntities.size() == 0) {
                return;
            }
            TestSetUploader testSetUploader = getTestSetUploader();
            if (testSetUploader == null) {
                return;
            }
            AlmRestTool almRestTool = new AlmRestTool(restClient, logger);
            params.put("actualUser", almRestTool.getActualUsername());
            testSetUploader.upload(xmlResultEntities);
        } else {
            logger.error("Login failed.");
        }
    }

    private void init() {
        cs = new CustomizationService(restClient, logger);
        vs = new VersionControlService(restClient, logger);
        udt = new UDFTranslator(cs, logger);
        rs = new RestService(restClient, logger, udt);
        fs = new FolderService(rs);
    }

    private TestSetUploader getTestSetUploader() {
        RunStatusMap runStatusMap = RunStatusMapLoader.load(params.get("runStatusMapping"), logger);
        if (runStatusMap == null) {
            return null;
        }
        RunUploader runu = new RunUploader(logger, params, rs, cs, runStatusMap.getStatus());
        TestInstanceUploader tiu = new TestInstanceUploader(logger, params, rs, runu, cs);
        TestUploader testu = new TestUploader(logger, params, rs, fs, tiu, cs, vs);
        return new TestSetUploader(logger, params, rs, fs, testu);
    }

    private List<XmlResultEntity> getUploadData() {
        EntitiesFieldMap entitiesFieldMap = EntitiesFieldMapLoader.load(params.get("fieldMapping"), logger, cs);
        if (entitiesFieldMap == null) {
            return null;
        }
        XmlReader xmlReader = new XmlReader(run, workspace, logger);
        List<XmlResultEntity> xmlResultEntities = xmlReader.scan(params.get("testingResultFile"), entitiesFieldMap);
        if (xmlResultEntities == null || xmlResultEntities.size() == 0) {
            logger.error("No test result content is found.");
        }
        return xmlResultEntities;
    }
}
