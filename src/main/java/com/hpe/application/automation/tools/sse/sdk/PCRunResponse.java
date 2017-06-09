package com.hpe.application.automation.tools.sse.sdk;

import com.hpe.application.automation.tools.sse.common.StringUtils;

/***
 * 
 * @author Effi Bar-She'an
 * @author Dani Schreiber
 * 
 */
public class PCRunResponse extends RunResponse {
    
    @Override
    protected String parseRunId(String runIdResponse) {
        String ret = runIdResponse;
        if (!StringUtils.isNullOrEmpty(ret)) {
            String runIdStr = "qcRunID=";
            if (ret.contains(runIdStr)) {
                ret = ret.substring(ret.indexOf(runIdStr) + runIdStr.length(), ret.length());
            }
        }
        return ret;
    }
    
}
