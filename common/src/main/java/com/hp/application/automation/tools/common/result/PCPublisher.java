package com.hp.application.automation.tools.common.result;

import com.hp.application.automation.tools.common.XPathUtils;
import com.hp.application.automation.tools.common.sdk.Client;
import com.hp.application.automation.tools.common.sdk.Logger;
import com.hp.application.automation.tools.common.sdk.Response;
import com.hp.application.automation.tools.common.sdk.request.GetPCRunEntityTestSetRunsRequest;
import com.hp.application.automation.tools.common.sdk.request.GetRequest;

/**
 * 
 * @author Effi Bar-She'an
 * @author Dani Schreiber
 * 
 */
public class PCPublisher extends Publisher {
    
    public PCPublisher(Client client, String entityId, String runId) {
        
        super(client, entityId, runId);
    }
    
    protected String getEntityName(String nameSuffix, Logger logger) {
        
        String ret = "Unnamed Entity";
        try {
            Response response = getEntityName(nameSuffix);
            if (response.isOk() && !response.toString().equals("")) {
                String runId = XPathUtils.getAttributeValue(response.toString(), "id");
                String testId = XPathUtils.getAttributeValue(response.toString(), "testcycl-id");
                String testSetId = XPathUtils.getAttributeValue(response.toString(), "cycle-id");
                ret =
                        String.format(
                                "PC Test ID: %s, Run ID: %s, Test Set ID: %s",
                                testId,
                                runId,
                                testSetId);
            } else {
                Throwable failure = response.getFailure();
                logger.log(String.format(
                        "Failed to get Entity name. Exception: %s",
                        failure == null ? "null" : failure.getMessage()));
            }
        } catch (Throwable e) {
            logger.log("Failed to get Entity name");
        }
        
        return ret;
    }
    
    @Override
    protected GetRequest getRunEntityTestSetRunsRequest(Client client, String runId) {
        
        return new GetPCRunEntityTestSetRunsRequest(client, runId);
    }
}
