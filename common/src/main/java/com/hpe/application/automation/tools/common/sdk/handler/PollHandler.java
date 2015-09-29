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

package com.hpe.application.automation.tools.common.sdk.handler;

import com.hpe.application.automation.tools.common.sdk.Client;
import com.hpe.application.automation.tools.common.sdk.Logger;
import com.hpe.application.automation.tools.common.sdk.Response;

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
