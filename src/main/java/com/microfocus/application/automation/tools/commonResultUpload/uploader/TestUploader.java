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
import com.microfocus.application.automation.tools.commonResultUpload.service.CustomizationService;
import com.microfocus.application.automation.tools.commonResultUpload.service.FolderService;
import com.microfocus.application.automation.tools.commonResultUpload.service.RestService;
import com.microfocus.application.automation.tools.commonResultUpload.service.VersionControlService;
import com.microfocus.application.automation.tools.commonResultUpload.xmlreader.model.XmlResultEntity;
import com.microfocus.application.automation.tools.results.service.almentities.AlmCommonProperties;
import com.microfocus.application.automation.tools.sse.sdk.Logger;
import org.apache.commons.lang.StringUtils;

import java.util.List;
import java.util.Map;

public class TestUploader {

    public static final String TEST_REST_PREFIX = "tests";
    private static final String TEST_FOLDERS_REST_PREFIX = "test-folders";
    public static final String[] NO_VERSION_TESTS = new String[]{"ALT-SCENARIO",
            "LEANFT-TEST", "LR-SCENARIO", "QAINSPECT-TEST"};

    private Map<String, String> params;
    private Logger logger;
    private RestService restService;
    private FolderService folderService;
    private CustomizationService customizationService;
    private TestInstanceUploader testInstanceUploader;
    private VersionControlService versionControlService;

    public TestUploader(Logger logger, Map<String, String> params,
                        RestService restService, FolderService folderService,
                        TestInstanceUploader testInstanceUploader,
                        CustomizationService customizationService,
                        VersionControlService versionControlService) {
        this.logger = logger;
        this.params = params;
        this.restService = restService;
        this.folderService = folderService;
        this.testInstanceUploader = testInstanceUploader;
        this.customizationService = customizationService;
        this.versionControlService = versionControlService;
    }

    public void upload(Map<String, String> testset, List<XmlResultEntity> xmlResultEntities) {
        for (XmlResultEntity xmlResultEntity : xmlResultEntities) {
            Map<String, String> test = xmlResultEntity.getValueMap();
            Map<String, String> newTest;

            if (!StringUtils.isEmpty(params.get("almTestFolder"))) {
                // Create or find a exists folder
                Map<String, String> folder = folderService.createOrFindPath(
                        TEST_FOLDERS_REST_PREFIX, "2", params.get("almTestFolder"));
                if (folder == null) {
                    continue;
                }

                // Find exists test under folder
                Map<String, String> existsTest = folderService.findEntityInFolder(folder, test,
                        TEST_REST_PREFIX, TEST_FOLDERS_REST_PREFIX,
                        new String[]{"id", "name", "subtype-id", "vc-version-number"});
                if (existsTest != null) {
                    newTest = existsTest;
                } else {
                    // If not, create the test under the folder
                    test.put(AlmCommonProperties.PARENT_ID, folder.get(AlmCommonProperties.ID));
                    newTest = restService.create(TEST_REST_PREFIX, test);
                }
            } else {
                // If no path was specified, put test under root
                test.put(AlmCommonProperties.PARENT_ID, "0");
                newTest = restService.create(TEST_REST_PREFIX, test);
            }

            if (newTest == null) {
                continue;
            } else {
                // upload test instance
                getVersionNumberForVC(newTest);
                test.putAll(newTest);
                testInstanceUploader.upload(testset, xmlResultEntity);
            }
        }
    }

    private void getVersionNumberForVC(Map<String, String> newTest) {
        // Some test type doesn't have version support
        for (String noVersionTest : NO_VERSION_TESTS) {
            if (newTest.get("subtype-id").equals(noVersionTest)) {
                return;
            }
        }

        boolean versioningEnabled = customizationService.isVersioningEnabled(
                CustomizationService.TEST_ENTITY_NAME);
        if (versioningEnabled && StringUtils.isEmpty(newTest.get("vc-version-number"))) {
            versionControlService.refreshEntityVersion(TEST_REST_PREFIX,
                    newTest.get(AlmCommonProperties.ID));

            newTest.putAll(restService.get(newTest.get(AlmCommonProperties.ID),
                    TEST_REST_PREFIX, CriteriaTranslator.getCriteriaString(
                            new String[]{"id", "name", "subtype-id", "vc-version-number"}, newTest)).get(0));
        }
    }
}
