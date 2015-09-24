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

package com.hp.application.automation.tools.common.sdk.handler;

import com.hp.application.automation.tools.common.StringUtils;
import com.hp.application.automation.tools.common.XPathUtils;
import com.hp.application.automation.tools.common.sdk.Client;
import com.hp.application.automation.tools.common.sdk.Logger;
import com.hp.application.automation.tools.common.sdk.Response;
import com.hp.application.automation.tools.common.sdk.request.EventLogRequest;

import java.util.List;
import java.util.Map;

public class EventLogHandler extends Handler {
    
    private String _timeslotId = StringUtils.EMPTY_STRING;
    private int _lastRead = -1;
    
    public EventLogHandler(Client client, String timeslotId) {
        
        super(client, timeslotId);
        _timeslotId = timeslotId;
    }
    
    public boolean log(Logger logger) {
        
        boolean ret = false;
        Response eventLog = null;
        try {
            eventLog = getEventLog();
            String xml = eventLog.toString();
            List<Map<String, String>> entities = XPathUtils.toEntities(xml);
            for (Map<String, String> currEntity : entities) {
                if (isNew(currEntity)) {
                    logger.log(String.format(
                            "%s:%s",
                            currEntity.get("creation-time"),
                            currEntity.get("description")));
                }
            }
            ret = true;
        } catch (Throwable cause) {
            logger.log(String.format(
                    "Failed to print Event Log: %s (run id: %s, reservation id: %s). Cause: %s",
                    eventLog,
                    _runId,
                    _timeslotId,
                    cause));
        }
        
        return ret;
    }
    
    private boolean isNew(Map<String, String> currEntity) {
        
        boolean ret = false;
        int currEvent = Integer.parseInt(currEntity.get("id"));
        if (currEvent > _lastRead) {
            _lastRead = currEvent;
            ret = true;
        }
        
        return ret;
    }
    
    private Response getEventLog() {
        
        return new EventLogRequest(_client, _timeslotId).execute();
    }
    
}
