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

import com.microfocus.application.automation.tools.commonResultUpload.service.CriteriaTranslator;
import com.microfocus.application.automation.tools.commonResultUpload.service.FolderService;
import com.microfocus.application.automation.tools.commonResultUpload.service.RestService;
import com.microfocus.application.automation.tools.commonResultUpload.xmlreader.model.XmlResultEntity;
import com.microfocus.application.automation.tools.results.service.almentities.AlmCommonProperties;
import com.microfocus.application.automation.tools.sse.sdk.Logger;
import org.apache.commons.lang.StringUtils;

import java.util.List;
import java.util.Map;

public class TestSetUploader {

    public static final String TEST_SET_REST_PREFIX = "test-sets";
    private static final String TEST_SET_FOLDERS_REST_PREFIX = "test-set-folders";

    private Map<String, String> params;
    private Logger logger;
    private RestService restService;
    private FolderService folderService;
    private TestUploader testuploader;

    public TestSetUploader(Logger logger, Map<String, String> params,
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
        for (XmlResultEntity xmlResultEntity : xmlResultEntities) {
            Map<String, String> testset = xmlResultEntity.getValueMap();

            // Find if there is test set with same name
            List<Map<String, String>> existTestsets = restService.get(null,
                    TEST_SET_REST_PREFIX,
                    CriteriaTranslator.getCriteriaString(
                            new String[]{"id", "name", "subtype-id"}, testset));

            if (existTestsets == null) {
                continue;
            }

            Map<String, String> newTestset;
            if (existTestsets.size() > 0) {
                newTestset = existTestsets.get(0);
            } else {
                // If no, create test set under folder
                if (!StringUtils.isEmpty(params.get("almTestSetFolder"))) {
                    Map<String, String> folder = folderService.createOrFindPath(
                            TEST_SET_FOLDERS_REST_PREFIX,
                            "0",
                            params.get("almTestSetFolder"));
                    if (folder == null) {
                        continue;
                    }
                    testset.put(AlmCommonProperties.PARENT_ID, folder.get(AlmCommonProperties.ID));
                } else {
                    // Or create test set under root
                    testset.put(AlmCommonProperties.PARENT_ID, "0");
                }
                newTestset = restService.create(TEST_SET_REST_PREFIX, testset);
            }

            if (newTestset == null) {
                continue;
            } else {
                testuploader.upload(newTestset, xmlResultEntity.getSubEntities());
            }
        }
    }
}
