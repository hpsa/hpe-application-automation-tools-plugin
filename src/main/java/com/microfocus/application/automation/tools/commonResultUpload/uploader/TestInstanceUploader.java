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
import com.microfocus.application.automation.tools.commonResultUpload.service.RestService;
import com.microfocus.application.automation.tools.commonResultUpload.xmlreader.model.XmlResultEntity;
import com.microfocus.application.automation.tools.results.service.almentities.AlmCommonProperties;
import com.microfocus.application.automation.tools.results.service.almentities.AlmTest;
import com.microfocus.application.automation.tools.results.service.almentities.AlmTestInstance;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TestInstanceUploader {

    private static final String TEST_CONFIG_PREFIX = "test-configs";
    public static final String TEST_INSTANCE_PREFIX = "test-instances";

    private CommonUploadLogger logger;
    private Map<String, String> params;
    private RestService restService;
    private RunUploader runUploader;
    private CustomizationService customizationService;

    public TestInstanceUploader(CommonUploadLogger logger, Map<String, String> params,
                                RestService restService, RunUploader runUploader,
                                CustomizationService customizationService) {
        this.logger = logger;
        this.params = params;
        this.restService = restService;
        this.runUploader = runUploader;
        this.customizationService = customizationService;
    }

    public void upload(Map<String, String> testset, XmlResultEntity xmlResultEntity) {
        Map<String, String> test = xmlResultEntity.getValueMap();
        Map<String, String> testconfig = getMainTestConfig(test);
        Map<String, String> testinstance;

        if (testconfig != null) {
            List<Map<String, String>> testInstances = getExistTestInstances(testset, test, testconfig);
            if (testInstances != null) {
                if (testInstances.size() == 0) {
                    testinstance = buildNewTestInstance(testset, test, testconfig);
                    testinstance.putAll(restService.create(TEST_INSTANCE_PREFIX, testinstance));
                } else {
                    testinstance = testInstances.get(0);
                }
                // Upload run
                if (xmlResultEntity.getSubEntities().size() > 0) {
                    runUploader.upload(testset, test, testconfig, testinstance,
                            xmlResultEntity.getSubEntities().get(0).getValueMap());
                } else {
                    logger.info("No run is found for test: " + test.get("name"));
                }
            }
        }
    }

    private Map<String, String> buildNewTestInstance(
            Map<String, String> testset,
            Map<String, String> test,
            Map<String, String> testconfig) {
        Map<String, String> testinstance = new HashMap<>();
        testinstance.put(AlmTestInstance.TEST_INSTANCE_SUBTYPE_ID,
                customizationService.getTestInstanceSubtypeIdByTest(test.get(AlmTest.TEST_TYPE)));
        testinstance.put(AlmTestInstance.TEST_INSTANCE_TESTSET_ID,
                String.valueOf(testset.get(AlmCommonProperties.ID)));
        testinstance.put(AlmTestInstance.TEST_INSTANCE_CONFIG_ID,
                String.valueOf(testconfig.get(AlmCommonProperties.ID)));
        testinstance.put(AlmTestInstance.TEST_INSTANCE_TEST_ID,
                String.valueOf(test.get(AlmCommonProperties.ID)));
        testinstance.put(AlmTestInstance.TEST_INSTANCE_TESTER_NAME,
                params.get("actualUser"));
        return testinstance;
    }

    private Map<String, String> getMainTestConfig(Map<String, String> test) {
        String queryString = String.format("query={parent-id[%s]}&fields=id,name",
                String.valueOf(test.get(AlmCommonProperties.ID)));
        List<Map<String, String>> testconfigs = restService.get(null, TEST_CONFIG_PREFIX, queryString);
        if (testconfigs != null && testconfigs.size() > 0) {
            return testconfigs.get(0);
        } else {
            return null;
        }
    }

    private List<Map<String, String>> getExistTestInstances(
            Map<String, String> testset,
            Map<String, String> test,
            Map<String, String> testconfig) {
        String queryString = String.format(
                "query={cycle-id[%s];test-config-id[%s];test-id[%s]}&fields=id,name,subtype-id",
                String.valueOf(testset.get(AlmCommonProperties.ID)),
                String.valueOf(testconfig.get(AlmCommonProperties.ID)),
                String.valueOf(test.get(AlmCommonProperties.ID)));
        return restService.get(null, TEST_INSTANCE_PREFIX, queryString);
    }
}
