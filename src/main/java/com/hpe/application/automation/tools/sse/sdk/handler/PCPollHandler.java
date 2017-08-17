package com.hpe.application.automation.tools.sse.sdk.handler;

import java.util.Arrays;
import java.util.List;

import com.hpe.application.automation.tools.sse.common.StringUtils;
import com.hpe.application.automation.tools.sse.common.XPathUtils;
import com.hpe.application.automation.tools.sse.sdk.Client;
import com.hpe.application.automation.tools.sse.sdk.Logger;
import com.hpe.application.automation.tools.sse.sdk.Response;
import com.hpe.application.automation.tools.sse.sdk.request.GetPCRunEntityDataRequest;

/***
 * 
 * @author Effi Bar-She'an
 * @author Dani Schreiber
 * 
 */
public class PCPollHandler extends PollHandler {
    
    private final static List<String> FINAL_STATES = Arrays.asList("N/A", "Failed", "Passed");
    
    public PCPollHandler(Client client, String entityId) {
        
        super(client, entityId);
    }
    
    public PCPollHandler(Client client, String entityId, int interval) {
        
        super(client, entityId, interval);
    }
    
    @Override
    protected Response getResponse() {
        
        return new GetPCRunEntityDataRequest(_client, _runId).execute();
    }
    
    @Override
    protected boolean isFinished(Response response, Logger logger) {
        
        boolean ret = false;
        try {
            String xml = response.toString();
            String pcEndTime = XPathUtils.getAttributeValue(xml, "pc-end-time");
            String status = XPathUtils.getAttributeValue(xml, "status");
            if (!StringUtils.isNullOrEmpty(pcEndTime)) {
                logger.log(String.format("PC test end time: %s", pcEndTime));
                ret = true;
            } else if (!StringUtils.isNullOrEmpty(status)) {
                if (FINAL_STATES.contains(status)) {
                    ret = true;
                }
            }
        } catch (Throwable cause) {
            logger.log(String.format("Failed to parse response: %s", response));
            ret = true;
        }
        
        return ret;
    }
    
    @Override
    protected boolean logRunEntityResults(Response response, Logger logger) {
        
        boolean ret = false;
        try {
            String xml = response.toString();
            String status = XPathUtils.getAttributeValue(xml, "status");
            String state = XPathUtils.getAttributeValue(xml, "state");
            logger.log(String.format("Run status of %s: %s, State: %s", _runId, status, state));
            ret = true;
            
        } catch (Throwable cause) {
            logger.log(String.format("Failed to parse response: %s", response));
        }
        
        return ret;
    }
    
    @Override
    protected Response getRunEntityResultsResponse() {
        
        return new GetPCRunEntityDataRequest(_client, _runId).execute();
    }
}
