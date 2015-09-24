/**
 © Copyright 2015 Hewlett Packard Enterprise Development LP

 Permission is hereby granted, free of charge, to any person obtaining a copy
 of this software and associated documentation files (the "Software"), to deal
 in the Software without restriction, including without limitation the rights
 to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 copies of the Software, and to permit persons to whom the Software is
 furnished to do so, subject to the following conditions:

 The above copyright notice and this permission notice shall be included in
 all copies or substantial portions of the Software.

 THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 FITNESS FOR A PARTICULAR PURPOSE AND NON INFRINGEMENT. IN NO EVENT SHALL THE
 AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 THE SOFTWARE.
 */

package com.hp.application.automation.tools.common.sdk.request;

import com.hp.application.automation.tools.common.sdk.Client;
import com.hp.application.automation.tools.common.sdk.ResourceAccessLevel;
import com.hp.application.automation.tools.common.sdk.Response;

public abstract class GeneralGetRequest extends GeneralRequest {
    
    protected GeneralGetRequest(Client client) {
        super(client);
    }
    
    protected String getQueryString() {
        
        return null;
    }
    
    @Override
    public Response perform() {
        
        return client.httpGet(
                getUrl(),
                getQueryString(),
                getHeaders(),
                ResourceAccessLevel.PROTECTED);
    }
}
