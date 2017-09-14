package com.hpe.application.automation.tools.sse.sdk.handler;

import com.hpe.application.automation.tools.common.SSEException;
import com.hpe.application.automation.tools.model.SseModel;
import com.hpe.application.automation.tools.sse.sdk.Client;

public class PollHandlerFactory {
    
    public PollHandler create(Client client, String runType, String entityId) {
        
        PollHandler ret = null;
        if ((SseModel.BVS.equals(runType)) || (SseModel.TEST_SET.equals(runType))) {
            ret = new LabPollHandler(client, entityId);
        } else if (SseModel.PC.equals(runType)) {
            ret = new PCPollHandler(client, entityId);
        } else {
            throw new SSEException("PollHandlerFactory: Unrecognized run type");
        }
        
        return ret;
    }
    
    public PollHandler create(Client client, String runType, String entityId, int interval) {
        
        PollHandler ret = null;
        if ((SseModel.BVS.equals(runType)) || (SseModel.TEST_SET.equals(runType))) {
            ret = new LabPollHandler(client, entityId, interval);
        } else if (SseModel.PC.equals(runType)) {
            ret = new PCPollHandler(client, entityId, interval);
        } else {
            throw new SSEException("PollHandlerFactory: Unrecognized run type");
        }
        
        return ret;
    }
}
