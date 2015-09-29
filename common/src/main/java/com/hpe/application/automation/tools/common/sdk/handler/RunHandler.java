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

import com.hpe.application.automation.tools.common.model.CdaDetails;
import com.hpe.application.automation.tools.common.sdk.Args;
import com.hpe.application.automation.tools.common.sdk.Client;
import com.hpe.application.automation.tools.common.sdk.Response;
import com.hpe.application.automation.tools.common.sdk.RunResponse;
import com.hpe.application.automation.tools.common.sdk.request.StartRunEntityRequest;
import com.hpe.application.automation.tools.common.sdk.request.StopEntityRequest;

public abstract class RunHandler extends Handler {
    
    protected abstract String getStartSuffix();
    
    public abstract String getNameSuffix();
    
    public RunHandler(Client client, String entityId) {
        
        super(client, entityId);
    }
    
    public Response start(
            String duration,
            String postRunAction,
            String environmentConfigurationId,
            CdaDetails cdaDetails) {
        
        return new StartRunEntityRequest(
                _client,
                getStartSuffix(),
                _entityId,
                duration,
                postRunAction,
                environmentConfigurationId,
                cdaDetails).execute();
    }
    
    public Response stop() {
        
        return new StopEntityRequest(_client, _runId).execute();
    }
    
    public String getReportUrl(Args args) {
        
        return _client.buildWebUIRequest(String.format("lab/index.jsp?processRunId=%s", _runId));
    }
    
    public RunResponse getRunResponse(Response response) {
        
        RunResponse ret = new RunResponse();
        ret.initialize(response);
        
        return ret;
        
    }
}
