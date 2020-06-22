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

import com.microfocus.adm.performancecenter.plugins.common.rest.RESTConstants;
import com.microfocus.application.automation.tools.commonResultUpload.CommonUploadLogger;
import com.microfocus.application.automation.tools.rest.RestClient;
import com.microfocus.application.automation.tools.sse.common.RestXmlUtils;
import com.microfocus.application.automation.tools.sse.common.XPathUtils;
import com.microfocus.application.automation.tools.sse.sdk.Response;

import java.util.HashMap;
import java.util.Map;

public abstract class BasicPostEntityRequest {

    private static final String START = "<Title>";
    private static final String END = "</Title>";
    private static final String IGNORE_REQUIRED_FIELDS_VALIDATION = "X-QC-Ignore-Customizable-Required-Fields-Validation";

    protected RestClient client;
    protected CommonUploadLogger logger;
    protected String operation;

    protected BasicPostEntityRequest(RestClient client, CommonUploadLogger logger, String operation) {
        this.client = client;
        this.logger = logger;
        this.operation = operation;
    }

    public abstract Map<String, String> perform(String restPrefix, Map<String, String> valueMap);

    protected Map<String, String> getHeaders() {
        Map<String, String> ret = new HashMap<String, String>();
        ret.put(RESTConstants.CONTENT_TYPE, RESTConstants.APP_XML);
        ret.put(RESTConstants.ACCEPT, RESTConstants.APP_XML);
        ret.put(IGNORE_REQUIRED_FIELDS_VALIDATION, "Y");
        return ret;
    }

    private String getRestErrorMessage(String responseContent) {
        return responseContent.substring(
                responseContent.indexOf(START) + START.length(),
                responseContent.indexOf(END)
        );
    }

    protected byte[] getDataBytes(Map<String, String> valueMap) {
        StringBuilder builder = new StringBuilder("<Entity><Fields>");
        for (Map.Entry<String, String> entry : valueMap.entrySet()) {
            builder.append(RestXmlUtils.fieldXml(entry.getKey(), entry.getValue()));
        }
        builder.append("</Fields></Entity>");
        logger.info("Request body: " + builder.toString());
        return builder.toString().getBytes();
    }

    private String getResultNameOrId(Map<String, String> result) {
        return result.get("name") != null ? result.get("name") : result.get("id");
    }

    protected Map<String, String> handleResult(Response response, Map<String, String> valueMap, String restPrefix) {
        if (response.isOk() && !response.toString().equals("")) {
            Map<String, String> result = XPathUtils.toEntities(response.toString()).get(0);
            logger.info(String.format("%s entity success. %s(%s)", operation, restPrefix,
                    getResultNameOrId(result)));
            return result;
        } else {
            logger.error(String.format("%s entity failed. %s(%s)", operation, restPrefix,
                    getResultNameOrId(valueMap)));
            logger.error(response.getFailure().toString());
            logger.error(getRestErrorMessage(response.toString()));
            return null;
        }
    }
}
