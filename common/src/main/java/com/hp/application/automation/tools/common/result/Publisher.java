package com.hp.application.automation.tools.common.result;

import com.hp.application.automation.tools.common.StringUtils;
import com.hp.application.automation.tools.common.XPathUtils;
import com.hp.application.automation.tools.common.result.model.junit.Testsuites;
import com.hp.application.automation.tools.common.sdk.Client;
import com.hp.application.automation.tools.common.sdk.Logger;
import com.hp.application.automation.tools.common.sdk.Response;
import com.hp.application.automation.tools.common.sdk.handler.Handler;
import com.hp.application.automation.tools.common.sdk.request.GetRequest;
import com.hp.application.automation.tools.common.sdk.request.GetRunEntityNameRequest;

import java.util.List;
import java.util.Map;

public abstract class Publisher extends Handler {
    
    public Publisher(Client client, String entityId, String runId) {
        
        super(client, entityId, runId);
    }
    
    public Testsuites publish(
            String nameSuffix,
            String url,
            String domain,
            String project,
            Logger logger) {
        
        Testsuites ret = null;
        GetRequest testSetRunsRequest = getRunEntityTestSetRunsRequest(_client, _runId);
        Response response = testSetRunsRequest.execute();
        List<Map<String, String>> testInstanceRun = getTestInstanceRun(response, logger);
        String entityName = getEntityName(nameSuffix, logger);
        if (testInstanceRun != null) {
            ret =
                    new JUnitParser().toModel(
                            testInstanceRun,
                            entityName,
                            _runId,
                            url,
                            domain,
                            project);
        }
        
        return ret;
    }
    
    protected Response getEntityName(String nameSuffix) {
        
        return new GetRunEntityNameRequest(_client, nameSuffix, _entityId).execute();
    }
    
    protected List<Map<String, String>> getTestInstanceRun(Response response, Logger logger) {
        
        List<Map<String, String>> ret = null;
        try {
            if (!StringUtils.isNullOrEmpty(response.toString())) {
                ret = XPathUtils.toEntities(response.toString());
            }
        } catch (Throwable cause) {
            logger.log(String.format(
                    "Failed to parse TestInstanceRuns response XML. Exception: %s, XML: %s",
                    cause.getMessage(),
                    response.toString()));
        }
        
        return ret;
    }
    
    protected abstract GetRequest getRunEntityTestSetRunsRequest(Client client, String runId);
    
    protected abstract String getEntityName(String nameSuffix, Logger logger);
}
