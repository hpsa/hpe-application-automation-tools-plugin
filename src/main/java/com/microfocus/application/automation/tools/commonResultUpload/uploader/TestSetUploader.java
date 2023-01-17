/*
 * Certain versions of software and/or documents ("Material") accessible here may contain branding from
 * Hewlett-Packard Company (now HP Inc.) and Hewlett Packard Enterprise Company.  As of September 1, 2017,
 * the Material is now offered by Micro Focus, a separately owned and operated company.  Any reference to the HP
 * and Hewlett Packard Enterprise/HPE marks is historical in nature, and the HP and Hewlett Packard Enterprise/HPE
 * marks are the property of their respective owners.
 * __________________________________________________________________
 * MIT License
 *
 * (c) Copyright 2012-2023 Micro Focus or one of its affiliates.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated
 * documentation files (the "Software"), to deal in the Software without restriction, including without limitation
 * the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software,
 * and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or
 * substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO
 * THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT,
 * TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 *
 * ___________________________________________________________________
 */

package com.microfocus.application.automation.tools.commonResultUpload.uploader;

import com.microfocus.application.automation.tools.commonResultUpload.CommonUploadLogger;
import com.microfocus.application.automation.tools.commonResultUpload.service.FolderService;
import com.microfocus.application.automation.tools.commonResultUpload.service.RestService;
import com.microfocus.application.automation.tools.commonResultUpload.xmlreader.model.XmlResultEntity;
import com.microfocus.application.automation.tools.results.service.AttachmentUploadService;
import org.apache.commons.lang.StringUtils;

import java.util.List;
import java.util.Map;

import static com.microfocus.application.automation.tools.commonResultUpload.ParamConstant.ALM_TESTSET_FOLDER;

public class TestSetUploader {

    public static final String TEST_SET_REST_PREFIX = "test-sets";
    private static final String TEST_SET_FOLDERS_REST_PREFIX = "test-set-folders";

    private Map<String, String> params;
    private CommonUploadLogger logger;
    private RestService restService;
    private FolderService folderService;
    private TestUploader testuploader;

    public TestSetUploader(CommonUploadLogger logger, Map<String, String> params,
                           RestService restService,
                           FolderService folderService,
                           TestUploader testuploader) {
        this.logger = logger;
        this.params = params;
        this.restService = restService;
        this.folderService = folderService;
        this.testuploader = testuploader;
    }

    public void upload(List<XmlResultEntity> xmlResultEntities) {
        logger.info("Test set upload start.");

        Map<String, String> folder = createOrFindTestsetFolder();

        for (XmlResultEntity xmlResultEntity : xmlResultEntities) {
            Map<String, String> testset = xmlResultEntity.getValueMap();

            // Find if there is test set with same name in the defined folder
            Map<String, String> existTestset = folderService.findEntityInFolder(folder, testset,
                    TEST_SET_REST_PREFIX, TEST_SET_FOLDERS_REST_PREFIX,
                    new String[]{"id", "name", "subtype-id"});

            uploadOrUpdateTestset(existTestset, testset, xmlResultEntity);
        }
    }

    private boolean uploadOrUpdateTestset(Map<String, String> existTestset,
                                          Map<String, String> testset, XmlResultEntity xmlResultEntity) {
        Map<String, String> newTestset;
        String attachemnt = null;

        if (existTestset != null) {
            // If yes, use the exist one to update.
            newTestset = existTestset;
        } else {
            // If no, create test set under folder
            attachemnt = testset.get("attachment");
            testset.remove("attachment");
            newTestset =  restService.create(TEST_SET_REST_PREFIX, testset);
        }

        if (newTestset == null) {
            return false;
        } else {
            if (StringUtils.isNotEmpty(attachemnt)) {
                AttachmentUploadService.getInstance().upload(attachemnt, TEST_SET_REST_PREFIX, newTestset.get("id"));
            }
            testuploader.upload(newTestset, xmlResultEntity.getSubEntities());
        }
        return true;
    }

    private Map<String, String> createOrFindTestsetFolder() {
        if (!StringUtils.isEmpty(params.get(ALM_TESTSET_FOLDER))) {
            return folderService.createOrFindPath(
                    TEST_SET_FOLDERS_REST_PREFIX,
                    "0",
                    params.get(ALM_TESTSET_FOLDER));
        } else {
            return null;
        }
    }
}
