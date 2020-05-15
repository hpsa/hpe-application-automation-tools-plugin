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

package com.microfocus.application.automation.tools.commonResultUpload.service;

import com.microfocus.application.automation.tools.commonResultUpload.CommonUploadLogger;
import com.microfocus.application.automation.tools.commonResultUpload.uploader.RunUploader;
import com.microfocus.application.automation.tools.commonResultUpload.uploader.TestSetUploader;
import com.microfocus.application.automation.tools.commonResultUpload.uploader.TestUploader;
import org.apache.commons.lang.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UDFTranslator {

    public static final String UDF_PREFIX = "udf|";
    private CommonUploadLogger logger;
    private CustomizationService customizationService;

    public UDFTranslator(CustomizationService customizationService, CommonUploadLogger logger) {
        this.customizationService = customizationService;
        this.logger = logger;
    }

    public void translate(String restPrefix, Map<String, String> valueMap) {
        switch (restPrefix) {
            case TestSetUploader.TEST_SET_REST_PREFIX:
                transUDFNames(CustomizationService.TEST_SET_ENTITY_NAME, valueMap);
                break;
            case TestUploader.TEST_REST_PREFIX:
                transUDFNames(CustomizationService.TEST_ENTITY_NAME, valueMap);
                break;
            case RunUploader.RUN_PREFIX:
                transUDFNames(CustomizationService.RUN_ENTITY_NAME, valueMap);
                break;
            default:
        }
    }

    private void transUDFNames(String entityName, Map<String, String> entityMap) {
        List<String> tobeRemoved = new ArrayList<>();
        Map<String, String> tobeAdded = new HashMap<>();

        for (String name : entityMap.keySet()) {
            if (name.startsWith(UDF_PREFIX)) {
                String label = name.substring(
                        name.indexOf(UDF_PREFIX) + UDF_PREFIX.length(),
                        name.length());
                String realName = customizationService.getUDFNameByLabel(entityName, label);
                if (StringUtils.isNotEmpty(realName)) {
                    tobeAdded.put(realName, entityMap.get(name));
                    tobeRemoved.add(name);
                } else {
                    logger.error(String.format("No user defined field with label [%s] was found.", label));
                }
            }
        }
        entityMap.putAll(tobeAdded);
        for (String item : tobeRemoved) {
            entityMap.remove(item);
        }
    }
}
