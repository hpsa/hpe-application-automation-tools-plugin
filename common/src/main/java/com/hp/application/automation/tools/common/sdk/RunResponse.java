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
