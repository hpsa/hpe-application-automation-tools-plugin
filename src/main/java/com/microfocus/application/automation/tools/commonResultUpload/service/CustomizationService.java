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
import com.microfocus.application.automation.tools.rest.RestClient;
import com.microfocus.application.automation.tools.sse.common.XPathUtils;
import com.microfocus.application.automation.tools.sse.sdk.ResourceAccessLevel;
import com.microfocus.application.automation.tools.sse.sdk.Response;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.util.HashMap;
import java.util.Map;

public class CustomizationService {

    public static final String RUN_ENTITY_NAME = "run";
    public static final String TEST_INSTANCE_ENTITY_NAME = "test-instance";
    public static final String TEST_ENTITY_NAME = "test";
    public static final String TEST_SET_ENTITY_NAME = "test-set";

    private RestClient client;
    private CommonUploadLogger logger;
    private Map<String, Map<String, String>> subtypeCache;
    private Map<String, Map<String, String>> fieldCache;

    public CustomizationService(RestClient client, CommonUploadLogger logger) {
        this.client = client;
        this.logger = logger;
        subtypeCache = new HashMap<>();
        fieldCache = new HashMap<>();
    }

    public String getRunSubtypeIdByTestInstance(String testInstanceSubtypeId) {
        return getSubtypeIdByName(RUN_ENTITY_NAME,
                getSubtypeNameById(TEST_INSTANCE_ENTITY_NAME, testInstanceSubtypeId));
    }

    public String getTestInstanceSubtypeIdByTest(String testSubtypeId) {
        return getSubtypeIdByName(TEST_INSTANCE_ENTITY_NAME,
                getSubtypeNameById(TEST_ENTITY_NAME, testSubtypeId));
    }

    public String getSubtypeIdByName(String entityName, String subtypeName) {
        return getEntitySubTypes(entityName).get(subtypeName);
    }

    public String getSubtypeNameById(String entityName, String subtypeId) {
        for (Map.Entry<String, String> subtype : getEntitySubTypes(entityName).entrySet()) {
            if (subtype.getValue().equals(subtypeId)) {
                return subtype.getKey();
            }
        }
        return null;
    }

    public Map<String, String> getEntitySubTypes(String entityName) {
        if (subtypeCache.get(entityName) == null) {
            String suffix = String.format("customization/entities/%s/types", entityName);
            String url = client.buildRestRequest(suffix);
            Response response = client.httpGet(
                    url,
                    null,
                    null,
                    ResourceAccessLevel.PROTECTED);
            if (response.isOk() && !response.toString().equals("")) {
                logger.info(String.format("Get customization entity subtypes success. [%s]", entityName));
                Map<String, String> customizationMap = XPathUtils.getEntitySubtypesMap(response.toString());
                subtypeCache.put(entityName, customizationMap);
                return customizationMap;
            } else {
                logger.error("Get customization entity subtypes failed from: " + url);
                logger.error(response.getFailure().toString());
                return null;
            }
        } else {
            return subtypeCache.get(entityName);
        }
    }

    public String getUDFNameByLabel(String entityName, String label) {
        return getEntityFields(entityName).get(label);
    }

    public Map<String, String> getEntityFields(String entityName) {
        if (fieldCache.get(entityName) == null) {
            String suffix = String.format("customization/entities/%s/fields", entityName);
            String url = client.buildRestRequest(suffix);
            Response response = client.httpGet(
                    url,
                    null,
                    null,
                    ResourceAccessLevel.PROTECTED);
            if (response.isOk() && !response.toString().equals("")) {
                logger.info(String.format("Get customization entity fields success. [%s]", entityName));
                Map<String, String> entityFieldsMap = XPathUtils.getEntityFieldsMap(response.toString());
                fieldCache.put(entityName, entityFieldsMap);
                return entityFieldsMap;
            } else {
                logger.error("Get customization entity fields failed from: " + url);
                logger.error(response.getFailure().toString());
                return null;
            }
        } else {
            return fieldCache.get(entityName);
        }
    }

    public boolean isVersioningEnabled(String entityName) {
        String suffix = String.format("customization/entities/%s", entityName);
        String url = client.buildRestRequest(suffix);
        Response response = client.httpGet(
                url,
                null,
                null,
                ResourceAccessLevel.PROTECTED);
        if (response.isOk() && !response.toString().equals("")) {
            logger.log(String.format("INFO: -- Get Entity Resource Descriptor success. [%s]", entityName));
            Document document = XPathUtils.getDocument(response.toString());
            Element element = (Element) document.getElementsByTagName("SupportsVC").item(0);
            return "true".equals(element.getTextContent());
        } else {
            logger.log("ERR: Get entities failed from: " + url);
            logger.log("ERR: " + response.getFailure());
            return false;
        }
    }
}
