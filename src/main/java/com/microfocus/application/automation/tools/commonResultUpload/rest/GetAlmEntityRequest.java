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

package com.microfocus.application.automation.tools.commonResultUpload.rest;

import com.microfocus.application.automation.tools.commonResultUpload.CommonUploadLogger;
import com.microfocus.application.automation.tools.rest.RestClient;
import com.microfocus.application.automation.tools.sse.common.XPathUtils;
import com.microfocus.application.automation.tools.sse.sdk.ResourceAccessLevel;
import com.microfocus.application.automation.tools.sse.sdk.Response;
import org.apache.commons.lang.StringUtils;

import java.util.List;
import java.util.Map;

public class GetAlmEntityRequest {

    private RestClient client;
    private CommonUploadLogger logger;

    public GetAlmEntityRequest(RestClient client, CommonUploadLogger logger) {
        this.client = client;
        this.logger = logger;
    }

    public List<Map<String, String>> perform(String id, String restPrefix, String queryString) {
        String suffix = StringUtils.isEmpty(id)
                ? restPrefix
                : String.format("%s/%s", restPrefix, id);
        String url = client.buildRestRequest(suffix);
        Response response = client.httpGet(
                url,
                queryString,
                null,
                ResourceAccessLevel.PROTECTED);
        if (response.isOk() && !response.toString().equals("")) {
            List<Map<String, String>> results = XPathUtils.toEntities(response.toString());
            /*List<String> names = new ArrayList<>();
            for (int i = 0; i < results.size(); i++) {
                names.add(results.get(i).get(AlmCommonProperties.NAME));
            }
            logger.info(String.format("Get entities success. %s(%s)", suffix, names.toString()));*/
            return results;
        } else {
            logger.error("Get entities failed from: " + url);
            logger.error(response.getFailure().toString());
            return null;
        }
    }
}
