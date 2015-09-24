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

package com.hp.application.automation.tools.common.sdk;

import com.hp.application.automation.tools.common.StringUtils;
import com.hp.application.automation.tools.common.XPathUtils;

/***
 * 
 * @author Effi Bar-She'an
 * @author Dani Schreiber
 * 
 */
public class RunResponse {
    
    private String _successStatus;
    private String _runId;
    
    public void initialize(Response response) {
        
        String xml = response.toString();
        _successStatus = XPathUtils.getAttributeValue(xml, "SuccessStaus");
        _runId = parseRunId(XPathUtils.getAttributeValue(xml, "info"));
    }
    
    protected String parseRunId(String runIdResponse) {
        
        String ret = runIdResponse;
        if (StringUtils.isNullOrEmpty(ret)) {
            ret = "No Run ID";
        }
        
        return ret;
    }
    
    public String getRunId() {
        
        return _runId;
    }
    
    public boolean isSucceeded() {
        
        return "1".equals(_successStatus);
    }
}