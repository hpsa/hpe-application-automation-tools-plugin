package com.hpe.application.automation.tools.sse.sdk.handler;

import com.hpe.application.automation.tools.sse.sdk.Client;
import com.hpe.application.automation.tools.sse.sdk.Logger;
import com.hpe.application.automation.tools.sse.sdk.Response;

public abstract class PollHandler extends Handler {
    
    private int _interval = 5000; // millisecond
    
    public PollHandler(Client client, String entityId) {
        
        super(client, entityId);
    }
    
    public PollHandler(Client client, String entityId, int interval) {
        
        super(client, entityId);
        _interval = interval;
    }
    
    public PollHandler(Client client, String entityId, String runId) {
        
        super(client, entityId, runId);
    }

    public boolean poll(Logger logger) throws InterruptedException {

        logger.log(String.format("Polling... Run ID: %s", _runId));

        return doPoll(logger);
    }
    
    protected boolean doPoll(Logger logger) throws InterruptedException {
        
        boolean ret = false;
        int failures = 0;
        while (failures < 3) {
            Response response = getResponse();
            if (isOk(response, logger)) {
                log(logger);
                if (isFinished(response, logger)) {
                    ret = true;
                    logRunEntityResults(getRunEntityResultsResponse(), logger);
                    break;
                }
            } else {
                ++failures;
            }
            if (sleep(logger)) { // interrupted
                break;
            }
        }
        
        return ret;
    }
    
    protected abstract Response getRunEntityResultsResponse();
    
    protected abstract boolean logRunEntityResults(Response response, Logger logger);
    
    protected abstract boolean isFinished(Response response, Logger logger);
    
    protected abstract Response getResponse();
    
    protected boolean isOk(Response response, Logger logger) {
        
        boolean ret = false;
        if (!response.isOk()) {
            Throwable cause = response.getFailure();
            logger.log(String.format(
                    "Polling try failed. Status code: %s, Exception: %s",
                    response.getStatusCode(),
                    cause != null ? cause.getMessage() : "Not Available"));
        } else {
            ret = true;
        }
        
        return ret;
    }
    
    protected boolean sleep(Logger logger) throws InterruptedException {
        
        boolean ret = false;
        try {
            Thread.sleep(_interval);
        } catch (InterruptedException ex) {
            logger.log("Interrupted while polling");
            throw ex;
        }
        
        return ret;
    }
    
    protected void log(Logger logger) {}
}
