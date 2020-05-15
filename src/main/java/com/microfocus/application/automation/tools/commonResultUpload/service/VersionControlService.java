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
import com.microfocus.application.automation.tools.sse.sdk.ResourceAccessLevel;
import com.microfocus.application.automation.tools.sse.sdk.Response;

public class VersionControlService  {

    public static final String CHECK_IN = "check-in";
    public static final String CHECK_OUT = "check-out";
    public static final String CHECK_UNDO_CHECK_OUT = "undo-check-out";

    private RestClient client;
    private CommonUploadLogger logger;

    public VersionControlService(RestClient client, CommonUploadLogger logger) {
        this.client = client;
        this.logger = logger;
    }

    /**
     * For new created entity, you can only get it's version number after check out and undo check out.
     * @param restPrefix
     * @param entityId
     * @return
     */
    public boolean refreshEntityVersion(String restPrefix, String entityId) {
        boolean result = false;
        result = versionsCheck(restPrefix, entityId, CHECK_OUT);
        if (!result) {
            return false;
        }
        return versionsCheck(restPrefix, entityId, CHECK_UNDO_CHECK_OUT);
    }

    /**
     * check in, check out, undo check out a entity
     * @param restPrefix
     * @param entityId
     * @param operation
     * @return
     */
    public boolean versionsCheck(String restPrefix, String entityId, String operation) {
        String suffix = String.format("%s/%s/versions/%s", restPrefix, entityId, operation);
        String url = client.buildRestRequest(suffix);
        Response response = client.httpPost(url, null, null, ResourceAccessLevel.PROTECTED);
        if (!response.isOk()) {
            logger.error(String.format("%s entity failed. %s(%s)", operation, restPrefix, entityId));
            logger.error(response.getFailure().toString());
            return false;
        } else {
            return true;
        }
    }
}
