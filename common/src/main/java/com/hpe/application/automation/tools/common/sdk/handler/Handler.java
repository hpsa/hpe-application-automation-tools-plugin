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

import com.hpe.application.automation.tools.common.StringUtils;
import com.hpe.application.automation.tools.common.sdk.Client;

public abstract class Handler {
    
    protected final Client _client;
    protected final String _entityId;
    protected String _runId = StringUtils.EMPTY_STRING;
    protected String _timeslotId = StringUtils.EMPTY_STRING;
    
    public Handler(Client client, String entityId) {
        
        _client = client;
        _entityId = entityId;
    }
    
    public Handler(Client client, String entityId, String runId) {
        
        this(client, entityId);
        _runId = runId;
    }
    
    public String getRunId() {
        
        return _runId;
    }
    
    public String getEntityId() {
        
        return _entityId;
    }
    
    public void setRunId(String runId) {
        _runId = runId;
    }
    
    public String getTimeslotId() {
        
        return _timeslotId;
    }
    
    public void setTimeslotId(String timeslotId) {
        
        _timeslotId = timeslotId;
    }
}
