package com.hpe.application.automation.tools.sse.sdk.handler;

import java.net.MalformedURLException;
import java.net.URL;

import com.hpe.application.automation.tools.common.SSEException;
import com.hpe.application.automation.tools.sse.sdk.Args;
import com.hpe.application.automation.tools.sse.sdk.Client;
import com.hpe.application.automation.tools.sse.sdk.PCRunResponse;
import com.hpe.application.automation.tools.sse.sdk.Response;
import com.hpe.application.automation.tools.sse.sdk.RunResponse;

/***
 * 
 * @author Effi Bar-She'an
 * @author Dani Schreiber
 * 
 */
public class PCRunHandler extends RunHandler {
    
    public PCRunHandler(Client client, String entityId) {
        
        super(client, entityId);
    }
    
    @Override
    protected String getStartSuffix() {
        
        return String.format("test-instances/%s/startrun", _entityId);
    }
    
    @Override
    public String getNameSuffix() {
        
        return String.format("runs/%s", _runId);
    }
    
    @Override
    public String getReportUrl(Args args) {
        
        String ret = "No report URL available";
        try {
            ret =
                    String.format(
                            "td://%s.%s.%s:8080/qcbin/[TestRuns]?EntityLogicalName=run&EntityID=%s",
                            args.getProject(),
                            args.getDomain(),
                            new URL(args.getUrl()).getHost(),
                            _runId);
        } catch (MalformedURLException ex) {
            throw new SSEException(ex);
        }
        return ret;
    }
    
    @Override
    public RunResponse getRunResponse(Response response) {
        
        RunResponse ret = new PCRunResponse();
        ret.initialize(response);
        
        return ret;
        
    }
}
